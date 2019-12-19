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

import com.intellij.codeInsight.daemon.LineMarkerProviders
import com.intellij.icons.AllIcons
import com.intellij.lang.Language
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import com.intellij.util.IconUtil
import motif.core.ResolvedGraph
import motif.intellij.ui.MotifErrorPanel
import motif.intellij.ui.MotifScopePanel
import motif.intellij.ui.MotifUsagePanel

class MotifProjectComponent(val project: Project) : ProjectComponent {

    companion object {
        const val TOOL_WINDOW_ID: String = "Motif"
        const val TOOL_WINDOW_TITLE: String = "Scopes"
        const val TAB_NAME_ERRORS: String = "Errors"
        const val TAB_NAME_SCOPES: String = "All Scopes"
        const val TAB_NAME_USAGE: String = "Usage"
        const val TAB_NAME_ANCESTOR: String = "Ancestors"
        const val LABEL_GRAPH_REFRESH: String = "Refreshing Motif Graph"

        val MOTIF_ACTION_IDS = listOf("motif_usage", "motif_graph")

        fun getInstance(project: Project): MotifProjectComponent {
            return project.getComponent(MotifProjectComponent::class.java)
        }
    }

    private val graphFactory: GraphFactory by lazy { GraphFactory(project) }
    private var scopePanel: MotifScopePanel? = null
    private var scopeContent: Content? = null
    private var errorPanel: MotifErrorPanel? = null
    private var errorContent: Content? = null
    private var usagePanel: MotifUsagePanel? = null
    private var usageContent: Content? = null
    private var ancestorPanel: MotifScopePanel? = null
    private var ancestorContent: Content? = null
    private var isRefreshing: Boolean = false

    override fun projectOpened() {
        DumbService.getInstance(project).runWhenSmart {
            ApplicationManager.getApplication().runReadAction {
                // Initialize plugin with empty graph to avoid IDE startup slowdown
                val emptyGraph: ResolvedGraph = ResolvedGraph.create(emptyList())
                onGraphUpdated(emptyGraph)
            }
        }
    }

    fun refreshGraph() {
        if (isRefreshing) {
            return
        }
        isRefreshing = true

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, LABEL_GRAPH_REFRESH) {
            override fun run(indicator: ProgressIndicator) {
                ApplicationManager.getApplication().runReadAction {
                    val updateGraph: ResolvedGraph = graphFactory.compute()
                    onGraphUpdated(updateGraph)
                    isRefreshing = false
                }
            }
        })
    }

    fun onSelectedClass(element: PsiElement) {
        if (element !is PsiClass) {
            return
        }
        val toolWindow: ToolWindow = ToolWindowManager.getInstance(project).getToolWindow(TOOL_WINDOW_ID) ?: return
        if (toolWindow.contentManager.findContent(TAB_NAME_USAGE) == null) {
            usageContent = createUsageContent(toolWindow)
        }
        usagePanel?.setSelectedClass(element)
        usageContent?.let { toolWindow.contentManager.setSelectedContent(it) }
    }

    fun onSelectedAncestorScope(element: PsiElement) {
        if (element !is PsiClass) {
            return
        }
        val toolWindow: ToolWindow = ToolWindowManager.getInstance(project).getToolWindow(TOOL_WINDOW_ID) ?: return
        if (toolWindow.contentManager.findContent(TAB_NAME_ANCESTOR) == null) {
            ancestorContent = createAncestorContent(toolWindow)
        }
        ancestorPanel?.setSelectedScope(element)
        ancestorContent?.let { toolWindow.contentManager.setSelectedContent(it) }
    }

    private fun onGraphUpdated(graph: ResolvedGraph) {
        ApplicationManager.getApplication().invokeLater {
            val toolWindowManager: ToolWindowManager = ToolWindowManager.getInstance(project)
            if (toolWindowManager.getToolWindow(TOOL_WINDOW_ID) == null) {
                val toolWindow: ToolWindow = toolWindowManager.registerToolWindow(TOOL_WINDOW_ID, true, ToolWindowAnchor.RIGHT)
                toolWindow.icon = IconLoader.getIcon("/icons/icon.svg")
                toolWindow.title = TOOL_WINDOW_TITLE

                scopePanel = MotifScopePanel(project, graph)
                errorPanel = MotifErrorPanel(project, graph)
                usagePanel = MotifUsagePanel(project, graph)
                ancestorPanel = MotifScopePanel(project, graph)

                scopeContent = createScopeContent(toolWindow)
                errorContent = createErrorContent(toolWindow)
            } else {
                scopePanel?.onGraphUpdated(graph)
                errorPanel?.onGraphUpdated(graph)
                usagePanel?.onGraphUpdated(graph)
                ancestorPanel?.onGraphUpdated(graph)
            }

            // display # of errors in tab label
            errorContent?.displayName = TAB_NAME_ERRORS + " (" + graph.errors.size + ")"

            // Propagate changes to line markers provider
            val language: Language? = Language.findLanguageByID("JAVA")
            language?.let {
                for (lineMarkerProvider in LineMarkerProviders.INSTANCE.allForLanguage(it)) {
                    if (lineMarkerProvider is Listener) {
                        lineMarkerProvider.onGraphUpdated(graph)
                    }
                }
            }

            // Propagate changes to actions
            MOTIF_ACTION_IDS.forEach {
                val usageAction: AnAction = ActionManager.getInstance().getAction(it)
                if (usageAction is Listener) {
                    usageAction.onGraphUpdated(graph)
                }
            }
        }
    }

    private fun createScopeContent(toolWindow: ToolWindow): Content {
        val content = ContentFactory.SERVICE.getInstance().createContent(scopePanel, TAB_NAME_SCOPES, true)
        content.isCloseable = false
        toolWindow.contentManager.addContent(content)
        return content
    }

    private fun createErrorContent(toolWindow: ToolWindow): Content {
        val content = ContentFactory.SERVICE.getInstance().createContent(errorPanel, TAB_NAME_ERRORS, true)
        content.isCloseable = false
        toolWindow.contentManager.addContent(content)
        return content
    }

    private fun createUsageContent(toolWindow: ToolWindow): Content {
        val content: Content = ContentFactory.SERVICE.getInstance().createContent(usagePanel, TAB_NAME_USAGE, true)
        content.isCloseable = true
        toolWindow.contentManager.addContent(content)
        return content
    }

    private fun createAncestorContent(toolWindow: ToolWindow): Content {
        val content: Content = ContentFactory.SERVICE.getInstance().createContent(ancestorPanel, TAB_NAME_ANCESTOR, true)
        content.isCloseable = true
        toolWindow.contentManager.addContent(content)
        return content
    }

    interface Listener {

        fun onGraphUpdated(graph: ResolvedGraph)
    }
}