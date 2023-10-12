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
import com.intellij.psi.PsiElement
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JSplitPane
import javax.swing.JTextArea
import motif.core.ResolvedGraph
import motif.errormessage.ErrorMessage
import motif.intellij.MotifService
import motif.intellij.ScopeHierarchyUtils
import motif.intellij.hierarchy.ErrorHierarchyBrowser
import motif.models.MotifError

class MotifErrorPanel(project: Project, graph: ResolvedGraph) :
    JPanel(), MotifService.Listener, ErrorHierarchyBrowser.Listener {

  private val splitPane: JSplitPane
  private val errorBrowser: ErrorHierarchyBrowser
  private val errorDetails: JTextArea

  init {
    val rootElement: PsiElement = ScopeHierarchyUtils.buildRootElement(project)

    // Build UI
    layout = BorderLayout()

    errorBrowser = ErrorHierarchyBrowser(project, graph, rootElement, this)
    errorBrowser.changeView(ErrorHierarchyBrowser.ERROR_HIERARCHY_TYPE)

    errorDetails = JTextArea()
    errorDetails.lineWrap = true
    errorDetails.isEditable = false
    errorDetails.isOpaque = false

    splitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT, errorBrowser, errorDetails)
    splitPane.dividerLocation = 250
    add(splitPane)
  }

  override fun onGraphUpdated(graph: ResolvedGraph) {
    errorBrowser.onGraphUpdated(graph)
    errorDetails.text = ""
  }

  override fun onSelectedErrorChanged(
      element: PsiElement,
      error: MotifError,
      errorMessage: ErrorMessage
  ) {
    errorDetails.text = errorMessage.text
  }
}
