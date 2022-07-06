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
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import motif.core.ResolvedGraph
import motif.intellij.MotifProjectComponent
import motif.intellij.MotifProjectComponent.Companion.TOOL_WINDOW_ID
import motif.intellij.ScopeHierarchyUtils.Companion.isInitializedGraph
import motif.intellij.analytics.AnalyticsProjectComponent
import motif.intellij.analytics.MotifAnalyticsActions

/*
 * {@AnAction} used to trigger displaying entire scope hierarchy.
 */
class MotifGraphAction : AnAction(), MotifProjectComponent.Listener {

  private var graph: ResolvedGraph? = null

  override fun onGraphUpdated(graph: ResolvedGraph) {
    this.graph = graph
  }

  override fun actionPerformed(event: AnActionEvent) {
    val project = event.project ?: return
    val graph = graph ?: return

    if (!isInitializedGraph(graph)) {
      MotifProjectComponent.getInstance(project).refreshGraph { actionPerformed(event) }
      return
    }

    val toolWindow: ToolWindow =
        ToolWindowManager.getInstance(project).getToolWindow(TOOL_WINDOW_ID) ?: return
    toolWindow.activate {}

    AnalyticsProjectComponent.getInstance(project).logEvent(MotifAnalyticsActions.GRAPH_MENU_CLICK)
  }

  override fun update(e: AnActionEvent) {
    e.presentation.isEnabled = true
  }
}
