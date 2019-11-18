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

import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import motif.core.ResolvedGraph
import motif.core.ScopeEdge
import motif.models.Scope
import java.util.*

class ScopeHierarchyUtils {

    companion object {
        fun buildRootElement(project: Project): PsiClass {
            return JavaPsiFacade.getInstance(project).findClass(Object::class.java.name, GlobalSearchScope.allScope(project))!!
        }

        fun isRootElement(element: PsiElement?): Boolean {
            return element is PsiClass && element.qualifiedName == Object::class.java.name
        }

        fun isMotifScopeClass(element: PsiClass?): Boolean {
            return element?.hasAnnotation(motif.Scope::class.java.name) ?: false
        }

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

        private fun getMotifScopePathsRecursive(scope: Scope, graph: ResolvedGraph, list: ArrayList<Scope>, all: ArrayList<ArrayList<Scope>>) {
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
}
