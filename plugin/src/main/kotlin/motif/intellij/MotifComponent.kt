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
package motif.intellij

import com.intellij.ide.hierarchy.LanguageTypeHierarchy
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiTreeChangeEvent
import motif.intellij.graph.GraphProcessor
import motif.intellij.hierarchy.MotifScopeHierarchyProvider
import motif.intellij.index.ScopeIndex
import motif.intellij.psi.PsiTreeChangeAdapter
import motif.intellij.psi.isMaybeScopeFile

class MotifComponent(private val project: Project) : ProjectComponent {

    val graphProcessor = GraphProcessor(project)

    private val scopeIndex = ScopeIndex.getInstance()

    init {
        println("MotifScopeHierarchyProvider addExplicitExtension")
        LanguageTypeHierarchy.INSTANCE.addExplicitExtension(JavaLanguage.INSTANCE, MotifScopeHierarchyProvider.INSTANCE)
    }

    override fun projectOpened() {
        PsiManager.getInstance(project).addPsiTreeChangeListener(object : PsiTreeChangeAdapter() {

            override fun onChange(event: PsiTreeChangeEvent) {
                val psiFile: PsiFile = event.file ?: return
                if (psiFile.isMaybeScopeFile()) {
                    scopeIndex.refreshFile(project, psiFile.virtualFile)
                }
            }
        })
    }

    companion object {

        fun get(project: Project): MotifComponent {
            return project.getComponent(MotifComponent::class.java)
        }
    }
}