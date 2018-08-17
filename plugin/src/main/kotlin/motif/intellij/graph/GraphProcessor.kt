/*
 * Copyright (c) 2018 Uber Technologies, Inc.
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
package motif.intellij.graph

import com.intellij.openapi.project.Project
import com.intellij.psi.*
import motif.intellij.index.ScopeIndex
import motif.intellij.psi.getClass
import motif.intellij.psi.getScopeClasses

class GraphProcessor(private val project: Project) {

    private val psiManager = PsiManager.getInstance(project)
    private val index = ScopeIndex.getInstance()

    /**
     * Returns a map of Scope -> List of Parent Scopes
     */
    fun scopeToParentsMap(): Map<PsiClass, List<PsiClass>> {
        return scopeMethods().groupBy({ it.childReturnType }) { it.scopeClass }
    }

    /**
     * Returns a map of Scope -> List of Children Scopes
     */
    fun scopeToChildrenMap(): Map<PsiClass, List<PsiClass>> {
        return scopeMethods().groupBy({ it.scopeClass }) { it.childReturnType }
    }

    private fun scopeMethods(): List<ScopeMethod> {
        val scopeClasses = scopeClasses()
        return scopeClasses
                .flatMap { scopeClass ->
                    scopeClass.methods
                            .mapNotNull { it.returnType?.getClass() }
                            .map { ScopeMethod(scopeClass, it) }
                }
                .filter { it.childReturnType in scopeClasses }
    }

    /**
     * Return a list of PsiClasses that are Motif Scopes.
     */
    fun scopeClasses(): List<PsiClass> {
        return index.getSnapshot(project).files
                .mapNotNull { psiManager.findFile(it) }
                .flatMap(PsiFile::getScopeClasses)
    }

    private data class ScopeMethod(val scopeClass: PsiClass, val childReturnType: PsiClass)
}