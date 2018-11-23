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
package motif.intellij.validation.ui

import com.intellij.icons.AllIcons.Process.State.GreenOK
import com.intellij.icons.AllIcons.Process.State.RedExcl
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentManager
import javax.swing.Icon

class MotifToolWindowFactory : ToolWindowFactory {

    override fun init(toolWindow: ToolWindow) {
        toolWindow.hide(null)

        var hasErrors = false
        GraphStateManager.addListener(toolWindow.contentManager) { state ->
            val icon: Icon = if (state.errors.isEmpty()) GreenOK else RedExcl
            toolWindow.icon = icon
            if (!hasErrors && state.errors.isNotEmpty()) {
                toolWindow.show(null)
            }
            hasErrors = state.errors.isNotEmpty()
        }
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentManager: ContentManager = toolWindow.contentManager
        val graphErrorPanel = GraphErrorPanel(project)
        contentManager.addContent(graphErrorPanel.content)
    }
}