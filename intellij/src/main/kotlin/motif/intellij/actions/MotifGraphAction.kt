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
package motif.intellij.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import motif.intellij.MotifProjectComponent
import motif.intellij.ScopeHierarchyUtils.Companion.isMotifScopeClass

/*
 * {@AnAction} used to trigger displaying  a particular scope ancestors hierarchy.
 */
class MotifGraphAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val element = event.getPsiElement() ?: return

        PsiDocumentManager.getInstance(project).commitAllDocuments()

        val toolWindow: ToolWindow = ToolWindowManager.getInstance(project).getToolWindow("Motif")
        toolWindow.activate {
            MotifProjectComponent.getInstance(project).onSelectedAncestorScope(element)
        }
    }

    override fun update(e: AnActionEvent) {
        val element: PsiElement? = e.getPsiElement()
        e.presentation.isEnabled = element is PsiClass && isMotifScopeClass(element)
    }

    private fun AnActionEvent.getPsiElement(): PsiElement? {
        return getData(CommonDataKeys.PSI_ELEMENT)
    }
}