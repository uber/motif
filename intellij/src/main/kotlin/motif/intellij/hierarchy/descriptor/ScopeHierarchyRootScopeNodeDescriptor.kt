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
package motif.intellij.hierarchy.descriptor

import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ui.util.CompositeAppearance
import com.intellij.psi.PsiElement
import motif.core.ResolvedGraph
import motif.intellij.hierarchy.ScopeHierarchyBrowser

class ScopeHierarchyRootDescriptor(
    project: Project,
    graph: ResolvedGraph,
    element: PsiElement,
    private val status: ScopeHierarchyBrowser.Status
) : ScopeHierarchyNodeDescriptor(project, graph, null, element, true) {

  companion object {
    const val LABEL_ROOT_SCOPES: String = "Root Scopes"
    const val LABEL_CLICK_REFRESH: String = "Click Refresh button to access Motif scope hierarchy."
    const val LABEL_WAIT: String = "Please wait..."
  }

  override fun updateText(text: CompositeAppearance) {
    when (status) {
      ScopeHierarchyBrowser.Status.UNINITIALIZED -> {
        text.ending.addText(LABEL_CLICK_REFRESH)
      }
      ScopeHierarchyBrowser.Status.INITIALIZING -> {
        text.ending.addText(LABEL_WAIT)
      }
      else -> {
        val textAttr: TextAttributes = getDefaultTextAttributes(graph.errors.isNotEmpty())
        text.ending.addText(LABEL_ROOT_SCOPES, textAttr)
      }
    }
  }
}
