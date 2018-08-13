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
import motif.intellij.psi.getScopeClasses

class GraphProcessor(private val project: Project) {

    private val psiManager = PsiManager.getInstance(project)
    private val index = ScopeIndex.getInstance()


    /**
     * Returns a map of Scope -> List of Parent Scopes
     */
    fun scopeClassesMap(): Map<PsiClass, List<PsiClass>> {

        // get a mapping of scopeClass -> scopesDeclared
        val scopeClasses = scopeClasses()
        return scopeClasses
                .flatMap { scopeClass ->
                    scopeClass.methods
                            .filter { it.returnType != null }
                            .map { ScopeMethod(scopeClass, getClass(it.returnType!!)) }
                }
                .filter { it.childReturnType in scopeClasses }
                .groupBy({ it.childReturnType }) {
                    it.scopeClass
                }
    }

    private data class ScopeMethod(val scopeClass: PsiClass, val childReturnType: PsiClass)

    /**
     * Return a list of PsiClasses that are Motif Scopes.
     */
    private fun scopeClasses(): List<PsiClass> {
        return index.getSnapshot(project).files
                .mapNotNull { psiManager.findFile(it) }
                .flatMap(PsiFile::getScopeClasses)
    }

    /**
     * Get a PsiClass from a PsiType
     */
    fun getClass(psiType: PsiType): PsiClass {
        val psiClassType = psiType as PsiClassType
        return psiClassType.resolve()!!
    }
}