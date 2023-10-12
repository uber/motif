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
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JSplitPane
import javax.swing.JSplitPane.RIGHT
import motif.core.ResolvedGraph
import motif.intellij.MotifService
import motif.intellij.ScopeHierarchyUtils
import motif.intellij.hierarchy.ScopeHierarchyBrowser
import motif.intellij.hierarchy.ScopePropertyHierarchyBrowser
import motif.intellij.hierarchy.ScopePropertyHierarchyBrowser.PropertyHierarchyType
import motif.models.Scope

class MotifScopePanel(val project: Project, initialGraph: ResolvedGraph) :
    JPanel(), ScopeHierarchyBrowser.Listener, MotifService.Listener {

  private var graph: ResolvedGraph = initialGraph

  private val splitPane: JSplitPane
  private val tabs: JBTabbedPane
  private val scopeBrowser: ScopeHierarchyBrowser
  private var consumeAndProvideBrowser: ScopePropertyHierarchyBrowser

  init {
    val rootElement: PsiElement = ScopeHierarchyUtils.buildRootElement(project)

    // Build UI
    layout = BorderLayout()
    scopeBrowser = ScopeHierarchyBrowser(project, graph, rootElement, this)
    scopeBrowser.changeView(ScopeHierarchyBrowser.TYPE_HIERARCHY_TYPE)

    consumeAndProvideBrowser =
        buildPropertyHierarchyBrowser(
            project, graph, rootElement, PropertyHierarchyType.CONSUME_AND_PROVIDE)

    tabs = JBTabbedPane()
    splitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT, scopeBrowser, consumeAndProvideBrowser)
    splitPane.dividerLocation = 250
    add(splitPane)
  }

  override fun onGraphUpdated(graph: ResolvedGraph) {
    this.graph = graph
    consumeAndProvideBrowser.onGraphUpdated(graph)
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
    val previousDividerLocation = splitPane.dividerLocation
    consumeAndProvideBrowser =
        buildPropertyHierarchyBrowser(
            project, graph, element, PropertyHierarchyType.CONSUME_AND_PROVIDE)
    splitPane.add(consumeAndProvideBrowser, RIGHT)
    splitPane.dividerLocation = previousDividerLocation
  }

  private fun buildPropertyHierarchyBrowser(
      project: Project,
      graph: ResolvedGraph,
      rootElement: PsiElement,
      type: PropertyHierarchyType
  ): ScopePropertyHierarchyBrowser {
    val propertyBrowser = ScopePropertyHierarchyBrowser(project, graph, rootElement, type)
    propertyBrowser.changeView(ScopePropertyHierarchyBrowser.PROPERTY_HIERARCHY_TYPE)
    return propertyBrowser
  }
}
