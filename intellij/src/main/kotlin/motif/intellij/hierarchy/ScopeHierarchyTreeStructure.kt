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
package motif.intellij.hierarchy

import com.intellij.ide.hierarchy.HierarchyNodeDescriptor
import com.intellij.ide.hierarchy.HierarchyTreeStructure
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementFactory
import com.intellij.psi.PsiType
import motif.ast.IrType
import motif.ast.intellij.IntelliJClass
import motif.ast.intellij.IntelliJMethod
import motif.ast.intellij.IntelliJType
import motif.core.ResolvedGraph
import motif.errormessage.ErrorMessage
import motif.intellij.ScopeHierarchyUtils
import motif.intellij.ScopeHierarchyUtils.Companion.MethodComparator
import motif.intellij.ScopeHierarchyUtils.Companion.ScopeComparator
import motif.intellij.ScopeHierarchyUtils.Companion.ScopeEdgeChildComparator
import motif.intellij.ScopeHierarchyUtils.Companion.ScopeEdgeParentComparator
import motif.intellij.ScopeHierarchyUtils.Companion.SinkComparator
import motif.intellij.ScopeHierarchyUtils.Companion.SourceComparator
import motif.intellij.ScopeHierarchyUtils.Companion.getVisibleSources
import motif.intellij.hierarchy.descriptor.ScopeHierarchyDependenciesSectionDescriptor
import motif.intellij.hierarchy.descriptor.ScopeHierarchyDependencyDescriptor
import motif.intellij.hierarchy.descriptor.ScopeHierarchyErrorDescriptor
import motif.intellij.hierarchy.descriptor.ScopeHierarchyRootDescriptor
import motif.intellij.hierarchy.descriptor.ScopeHierarchyRootErrorDescriptor
import motif.intellij.hierarchy.descriptor.ScopeHierarchyScopeAncestorDescriptor
import motif.intellij.hierarchy.descriptor.ScopeHierarchyScopeDescriptor
import motif.intellij.hierarchy.descriptor.ScopeHierarchySimpleDescriptor
import motif.intellij.hierarchy.descriptor.ScopeHierarchySinkDescriptor
import motif.intellij.hierarchy.descriptor.ScopeHierarchySinkDetailsDescriptor
import motif.intellij.hierarchy.descriptor.ScopeHierarchySinksSectionDescriptor
import motif.intellij.hierarchy.descriptor.ScopeHierarchySourceDescriptor
import motif.intellij.hierarchy.descriptor.ScopeHierarchySourceDetailsDescriptor
import motif.intellij.hierarchy.descriptor.ScopeHierarchySourcesAndSinksSectionDescriptor
import motif.intellij.hierarchy.descriptor.ScopeHierarchySourcesSectionDescriptor
import motif.intellij.hierarchy.descriptor.ScopeHierarchyUsageSectionDescriptor
import motif.intellij.hierarchy.descriptor.ScopeHierarchyUsageSinksSectionDescriptor
import motif.intellij.hierarchy.descriptor.ScopeHierarchyUsageSourcesSectionDescriptor
import motif.models.Dependencies

class ScopeHierarchyTreeStructure(
    val project: Project,
    val graph: ResolvedGraph,
    descriptor: HierarchyNodeDescriptor
) : HierarchyTreeStructure(project, descriptor) {

  companion object {
    const val LABEL_SCOPE_NO_PROVIDE: String = "This Scope does not provide any object."
    const val LABEL_SCOPE_NO_CONSUME: String = "This Scope does not consume any object."
    const val LABEL_SCOPE_NO_DEPENDENCIES: String = "This Scope does not have any dependencies."
  }

  init {
    setBaseElement(descriptor)
  }

  override fun buildChildren(descriptor: HierarchyNodeDescriptor): Array<Any> {
    val descriptors: ArrayList<HierarchyNodeDescriptor> = ArrayList(1)
    when (descriptor) {
      is ScopeHierarchyRootDescriptor -> {
        graph.roots.sortedWith(ScopeComparator).forEach { scope ->
          descriptors.add(
              ScopeHierarchyScopeDescriptor(
                  myProject, graph, descriptor, (scope.clazz as IntelliJClass).psiClass, scope))
        }
      }
      is ScopeHierarchyScopeAncestorDescriptor -> {
        graph.getParentEdges(descriptor.scope).sortedWith(ScopeEdgeParentComparator).forEach { edge
          ->
          descriptors.add(
              ScopeHierarchyScopeAncestorDescriptor(
                  myProject,
                  graph,
                  descriptor,
                  (edge.parent.clazz as IntelliJClass).psiClass,
                  edge.parent))
        }
      }
      is ScopeHierarchyScopeDescriptor -> {
        graph.getChildEdges(descriptor.scope).sortedWith(ScopeEdgeChildComparator).forEach { edge ->
          descriptors.add(
              ScopeHierarchyScopeDescriptor(
                  myProject,
                  graph,
                  descriptor,
                  (edge.child.clazz as IntelliJClass).psiClass,
                  edge.child))
        }
      }
      is ScopeHierarchySourcesSectionDescriptor -> {
        getVisibleSources(graph, descriptor.scope).sortedWith(SourceComparator).forEach { source ->
          descriptors.add(ScopeHierarchySourceDescriptor(myProject, graph, descriptor, source))
        }
        if (descriptors.isEmpty()) {
          descriptors.add(
              ScopeHierarchySimpleDescriptor(
                  myProject, graph, descriptor, descriptor.element, LABEL_SCOPE_NO_PROVIDE))
        }
      }
      is ScopeHierarchySinksSectionDescriptor -> {
        graph.getSinks(descriptor.scope).sortedWith(SinkComparator).forEach { sink ->
          descriptors.add(ScopeHierarchySinkDescriptor(myProject, graph, descriptor, sink))
        }
        if (descriptors.isEmpty()) {
          descriptors.add(
              ScopeHierarchySimpleDescriptor(
                  myProject, graph, descriptor, descriptor.element, LABEL_SCOPE_NO_CONSUME))
        }
      }
      is ScopeHierarchySourcesAndSinksSectionDescriptor -> {
        descriptors.add(
            ScopeHierarchySourcesSectionDescriptor(
                myProject, graph, descriptor, descriptor.element, descriptor.scope, true))
        descriptors.add(
            ScopeHierarchySinksSectionDescriptor(
                myProject, graph, descriptor, descriptor.element, descriptor.scope, true))
      }
      is ScopeHierarchyDependenciesSectionDescriptor -> {
        val dependencies: Dependencies? = descriptor.scope.dependencies
        if (dependencies != null) {
          dependencies.methods.sortedWith(MethodComparator).forEach { method ->
            descriptors.add(
                ScopeHierarchyDependencyDescriptor(
                    myProject,
                    graph,
                    descriptor,
                    (method.method as IntelliJMethod).psiMethod,
                    method))
          }
        }
        if (descriptors.isEmpty()) {
          descriptors.add(
              ScopeHierarchySimpleDescriptor(
                  myProject,
                  graph,
                  descriptor,
                  descriptor.psiElement!!,
                  LABEL_SCOPE_NO_DEPENDENCIES))
        }
      }
      is ScopeHierarchySinkDetailsDescriptor -> {
        // returns no children
      }
      is ScopeHierarchySourceDetailsDescriptor -> {
        // returns no children
      }
      is ScopeHierarchySinkDescriptor -> {
        graph.getProviders(descriptor.sink).sortedWith(SourceComparator).forEach { source ->
          descriptors.add(
              ScopeHierarchySourceDetailsDescriptor(myProject, graph, descriptor, source))
        }
      }
      is ScopeHierarchySourceDescriptor -> {
        graph.getConsumers(descriptor.source).sortedWith(SinkComparator).forEach { sink ->
          descriptors.add(ScopeHierarchySinkDetailsDescriptor(myProject, graph, descriptor, sink))
        }
      }
      is ScopeHierarchyRootErrorDescriptor -> {
        graph.errors.forEach { error ->
          val errorMessage: ErrorMessage = ErrorMessage.get(error)
          descriptors.add(
              ScopeHierarchyErrorDescriptor(myProject, graph, descriptor, error, errorMessage))
        }
      }
      is ScopeHierarchyUsageSectionDescriptor -> {
        val countSources: Int =
            ScopeHierarchyUtils.getUsageCount(
                project, graph, descriptor.clazz, includeSources = true, includeSinks = false)
        val countSinks: Int =
            ScopeHierarchyUtils.getUsageCount(
                project, graph, descriptor.clazz, includeSources = false, includeSinks = true)
        if (countSources > 0) {
          descriptors.add(
              ScopeHierarchyUsageSourcesSectionDescriptor(
                  myProject, graph, descriptor, descriptor.clazz))
        }
        if (countSinks > 0) {
          descriptors.add(
              ScopeHierarchyUsageSinksSectionDescriptor(
                  myProject, graph, descriptor, descriptor.clazz))
        }
      }
      is ScopeHierarchyUsageSourcesSectionDescriptor -> {
        val elementType: PsiType =
            PsiElementFactory.SERVICE.getInstance(project).createType(descriptor.clazz)
        val type: IrType = IntelliJType(project, elementType)
        graph.getSources(type).sortedWith(SourceComparator).forEach { source ->
          descriptors.add(
              ScopeHierarchySourceDetailsDescriptor(myProject, graph, descriptor, source))
        }
      }
      is ScopeHierarchyUsageSinksSectionDescriptor -> {
        val elementType: PsiType =
            PsiElementFactory.SERVICE.getInstance(project).createType(descriptor.clazz)
        val type: IrType = IntelliJType(project, elementType)
        graph.getSinks(type).sortedWith(SinkComparator).forEach { sink ->
          descriptors.add(ScopeHierarchySinkDetailsDescriptor(myProject, graph, descriptor, sink))
        }
      }
    }
    return descriptors.toTypedArray()
  }
}
