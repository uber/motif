/*
 * Copyright (c) 2018-2019 Uber Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package motif.intellij

import com.google.common.collect.Iterables
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementFactory
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiType
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import motif.ast.IrType
import motif.ast.intellij.IntelliJType
import motif.core.ResolvedGraph
import motif.core.ScopeEdge
import motif.models.ChildParameterSource
import motif.models.Dependencies
import motif.models.Scope
import motif.models.ScopeSource
import motif.models.Sink
import motif.models.Source

object ScopeHierarchyUtils {

  object ScopeComparator : Comparator<Scope> {
    override fun compare(o1: Scope, o2: Scope): Int = o1.simpleName.compareTo(o2.simpleName)
  }

  object ScopeEdgeParentComparator : Comparator<ScopeEdge> {
    override fun compare(o1: ScopeEdge, o2: ScopeEdge): Int =
        o1.parent.simpleName.compareTo(o2.parent.simpleName)
  }

  object ScopeEdgeChildComparator : Comparator<ScopeEdge> {
    override fun compare(o1: ScopeEdge, o2: ScopeEdge): Int =
        o1.child.simpleName.compareTo(o2.child.simpleName)
  }

  object SourceComparator : Comparator<Source> {
    override fun compare(o1: Source, o2: Source): Int {
      if (o1.isExposed != o2.isExposed) return o1.isExposed.compareTo(o2.isExposed) * -1
      return o1.type.simpleName.compareTo(o2.type.simpleName)
    }
  }

  object SinkComparator : Comparator<Sink> {
    override fun compare(o1: Sink, o2: Sink): Int = o1.type.simpleName.compareTo(o2.type.simpleName)
  }

  object MethodComparator : Comparator<Dependencies.Method> {
    override fun compare(o1: Dependencies.Method, o2: Dependencies.Method): Int =
        o1.method.name.compareTo(o2.method.name)
  }

  fun buildRootElement(project: Project): PsiClass =
      JavaPsiFacade.getInstance(project)
          .findClass(Object::class.java.name, GlobalSearchScope.allScope(project))!!

  fun isRootElement(element: PsiElement?): Boolean =
      element is PsiClass && element.qualifiedName == Object::class.java.name

  fun isInitializedGraph(graph: ResolvedGraph): Boolean = graph.roots.isNotEmpty()

  fun isMotifScopeClass(element: PsiClass?): Boolean =
      element?.hasAnnotation(motif.Scope::class.java.name) ?: false

  fun isMotifChildScopeMethod(element: PsiElement?): Boolean {
    if (element is PsiMethod) {
      val classElement = PsiTreeUtil.getParentOfType(element, PsiClass::class.java)
      if (isMotifScopeClass(classElement) && element.returnType is PsiClassReferenceType) {
        val returnElementClass: PsiClass? = (element.returnType as PsiClassReferenceType).resolve()
        if (isMotifScopeClass(returnElementClass)) {
          return true
        }
      }
    }
    return false
  }

  fun getParentScopes(
      project: Project,
      graph: ResolvedGraph,
      element: PsiClass,
  ): Array<ScopeEdge>? {
    val scopeType: PsiType = PsiElementFactory.SERVICE.getInstance(project).createType(element)
    val type: IrType = IntelliJType(project, scopeType)
    val scope: Scope? = graph.getScope(type)
    return if (scope != null) {
      Iterables.toArray(graph.getParentEdges(scope), ScopeEdge::class.java) as Array<ScopeEdge>?
    } else {
      null
    }
  }

  /*
   * Returns the list of sources for given scope to display in the UI
   */
  fun getVisibleSources(graph: ResolvedGraph, scope: Scope): List<Source> =
      graph.getSources(scope).filter { it !is ChildParameterSource && it !is ScopeSource }

  /*
   * Returns the number of usage for the given class.
   */
  fun getUsageCount(
      project: Project,
      graph: ResolvedGraph,
      clazz: PsiClass,
      includeSources: Boolean = true,
      includeSinks: Boolean = true,
  ): Int {
    var count = 0
    val elementType: PsiType = PsiElementFactory.SERVICE.getInstance(project).createType(clazz)
    val type: IrType = IntelliJType(project, elementType)
    if (includeSources) {
      graph.getSources(type).forEach { _ -> count++ }
    }
    if (includeSinks) {
      graph.getSinks(type).forEach { _ -> count++ }
    }
    return count
  }

  fun getUsageString(count: Int): String =
      when (count) {
        0 -> "No usage"
        1 -> "1 usage"
        else -> "$count usages"
      }

  fun getObjectString(count: Int): String =
      when (count) {
        0 -> "No object"
        1 -> "1 object"
        else -> "$count objects"
      }

  fun formatQualifiedName(qualifiedName: String): String {
    val index: Int = qualifiedName.lastIndexOf(".")
    return if (index > 0) qualifiedName.substring(0, index) else qualifiedName
  }

  fun formatMultilineText(text: String): String = "<html>" + text.replace("\n", "<br>") + "</html>"

  /*
   * Returns all the paths, starting from root scopes, leading to the provided scope.
   */
  fun getMotifScopePaths(scope: Scope, graph: ResolvedGraph): ArrayList<ArrayList<Scope>> {
    val list: ArrayList<Scope> = ArrayList()
    val all: ArrayList<ArrayList<Scope>> = ArrayList()
    all.add(list)
    getMotifScopePathsRecursive(scope, graph, list, all)
    return all
  }

  private fun getMotifScopePathsRecursive(
      scope: Scope,
      graph: ResolvedGraph,
      list: ArrayList<Scope>,
      all: ArrayList<ArrayList<Scope>>,
  ) {
    list.add(scope)
    val parentEdgesIterator: Iterator<ScopeEdge> = graph.getParentEdges(scope).iterator()
    if (parentEdgesIterator.hasNext()) {
      getMotifScopePathsRecursive(parentEdgesIterator.next().parent, graph, list, all)
    }
    for (edge: ScopeEdge in parentEdgesIterator) {
      val newList: ArrayList<Scope> = ArrayList()
      all.add(newList)
      getMotifScopePathsRecursive(edge.parent, graph, newList, all)
    }
  }
}
