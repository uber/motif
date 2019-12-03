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
package motif.intellij.ui

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import motif.core.ResolvedGraph
import motif.intellij.MotifProjectComponent
import motif.intellij.ScopeHierarchyUtils
import motif.intellij.hierarchy.UsageHierarchyBrowser
import java.awt.BorderLayout
import javax.swing.JPanel

class MotifUsagePanel(project: Project, graph: ResolvedGraph) : JPanel(), MotifProjectComponent.Listener {

    private val usageBrowser: UsageHierarchyBrowser

    init {
        val rootElement: PsiElement = ScopeHierarchyUtils.buildRootElement(project)

        // Build UI
        layout = BorderLayout()

        usageBrowser = UsageHierarchyBrowser(project, graph, rootElement)
        usageBrowser.changeView(UsageHierarchyBrowser.USAGE_HIERARCHY_TYPE)

        add(usageBrowser)
    }

    fun setSelectedClass(clazz: PsiClass) {
        usageBrowser.setSelectedClass(clazz)
    }

    override fun onGraphUpdated(graph: ResolvedGraph) {
        usageBrowser.onGraphUpdated(graph)
    }
}

