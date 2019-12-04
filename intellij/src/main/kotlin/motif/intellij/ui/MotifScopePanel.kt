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
import com.intellij.ui.components.JBTabbedPane
import motif.core.ResolvedGraph
import motif.intellij.MotifProjectComponent
import motif.intellij.hierarchy.ScopePropertyHierarchyBrowser
import motif.intellij.hierarchy.ScopePropertyHierarchyBrowser.PropertyHierarchyType
import motif.intellij.hierarchy.ScopeHierarchyBrowser
import motif.intellij.ScopeHierarchyUtils
import motif.models.Scope
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.JPanel
import javax.swing.JSplitPane

class MotifScopePanel(project: Project, graph: ResolvedGraph) : JPanel(), ScopeHierarchyBrowser.Listener, MotifProjectComponent.Listener {

    companion object {
        const val USE_TABS: Boolean = false;
        const val TAB_NAME_CONSUMES: String = "Consumes"
        const val TAB_NAME_PROVIDES: String = "Provides"
        const val TAB_NAME_DEPENDENCIES: String = "Dependencies"
    }

    private val splitPane: JSplitPane
    private val tabs: JBTabbedPane
    private val scopeBrowser: ScopeHierarchyBrowser
    private val provideBrowser: ScopePropertyHierarchyBrowser
    private val consumeBrowser: ScopePropertyHierarchyBrowser
    private val consumeAndProvideBrowser: ScopePropertyHierarchyBrowser
    private val dependenciesBrowser: ScopePropertyHierarchyBrowser

    init {
        val rootElement: PsiElement = ScopeHierarchyUtils.buildRootElement(project)

        // Build UI
        layout = BorderLayout()
        scopeBrowser = ScopeHierarchyBrowser(project, graph, rootElement, this)
        scopeBrowser.changeView(ScopeHierarchyBrowser.TYPE_HIERARCHY_TYPE)

        consumeBrowser = buildPropertyHierarchyBrowser(project, graph, rootElement, PropertyHierarchyType.CONSUME)
        provideBrowser = buildPropertyHierarchyBrowser(project, graph, rootElement, PropertyHierarchyType.PROVIDE)
        consumeAndProvideBrowser = buildPropertyHierarchyBrowser(project, graph, rootElement, PropertyHierarchyType.CONSUME_AND_PROVIDE)
        dependenciesBrowser = buildPropertyHierarchyBrowser(project, graph, rootElement, PropertyHierarchyType.DEPENDENCIES)

        tabs = JBTabbedPane()
        tabs.addTab(TAB_NAME_CONSUMES, consumeBrowser)
        tabs.addTab(TAB_NAME_PROVIDES, provideBrowser)
        tabs.addTab(TAB_NAME_DEPENDENCIES, dependenciesBrowser)

        val bottomComponent: Component = if (USE_TABS) tabs else consumeAndProvideBrowser
        splitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT, scopeBrowser, bottomComponent)
        splitPane.dividerLocation = 250
        add(splitPane)
    }

    override fun onGraphUpdated(graph: ResolvedGraph) {
        provideBrowser.onGraphUpdated(graph)
        consumeBrowser.onGraphUpdated(graph)
        consumeAndProvideBrowser.onGraphUpdated(graph)
        dependenciesBrowser.onGraphUpdated(graph)
        scopeBrowser.onGraphUpdated(graph)
    }

    /*
     * Request to select a given scope in the scope hierarchy browser.
     */
    fun setSelectedScope(clazz: PsiClass) {
        scopeBrowser.setSelectedScope(clazz)
    }

    /*
     * Notify panel that a new scope has been selected in scope hierarchy browser.
     */
    override fun onSelectedScopeChanged(element: PsiElement, scope: Scope) {
        provideBrowser.setSelectedScope(element)
        consumeBrowser.setSelectedScope(element)
        consumeAndProvideBrowser.setSelectedScope(element)
        dependenciesBrowser.setSelectedScope(element)
    }

    private fun buildPropertyHierarchyBrowser(project: Project, graph: ResolvedGraph, rootElement: PsiElement, type: PropertyHierarchyType): ScopePropertyHierarchyBrowser {
        val propertyBrowser = ScopePropertyHierarchyBrowser(project, graph, rootElement, type)
        propertyBrowser.changeView(ScopePropertyHierarchyBrowser.PROPERTY_HIERARCHY_TYPE)
        return propertyBrowser
    }
}

