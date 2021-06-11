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
import motif.core.ResolvedGraph
import motif.intellij.MotifProjectComponent
import motif.intellij.MotifProjectComponent.Companion.TOOL_WINDOW_ID
import motif.intellij.ScopeHierarchyUtils
import motif.intellij.ScopeHierarchyUtils.Companion.getUsageCount
import motif.intellij.analytics.AnalyticsProjectComponent
import motif.intellij.analytics.MotifAnalyticsActions

/*
 * {@AnAction} used to trigger navigation to a particular scope in the scope hierarchy window.
 */
class MotifUsageAction : AnAction(), MotifProjectComponent.Listener {

    private var graph: ResolvedGraph? = null

    override fun onGraphUpdated(graph: ResolvedGraph) {
        this.graph = graph
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val element = event.getPsiElement() ?: return
        val graph = graph ?: return

        if (!ScopeHierarchyUtils.isInitializedGraph(graph)) {
            MotifProjectComponent.getInstance(project).refreshGraph { actionPerformed(event) }
            return
        }

        val toolWindow: ToolWindow = ToolWindowManager.getInstance(project).getToolWindow(TOOL_WINDOW_ID) ?: return
        toolWindow.activate {
            MotifProjectComponent.getInstance(project).onSelectedClass(element)
        }

        AnalyticsProjectComponent.getInstance(project).logEvent(MotifAnalyticsActions.USAGE_MENU_CLICK)
    }

    override fun update(e: AnActionEvent) {
        val project = e.project ?: return
        val graph = this.graph ?: return
        val element: PsiElement? = e.getPsiElement()
        e.presentation.isEnabled = element is PsiClass && (!ScopeHierarchyUtils.isInitializedGraph(graph) || getUsageCount(project, graph, element) > 0)
    }

    private fun AnActionEvent.getPsiElement(): PsiElement? {
        return getData(CommonDataKeys.PSI_ELEMENT)
    }
}