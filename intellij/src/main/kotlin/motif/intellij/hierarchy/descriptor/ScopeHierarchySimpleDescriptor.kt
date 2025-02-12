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

import com.intellij.ide.hierarchy.HierarchyNodeDescriptor
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ui.util.CompositeAppearance
import com.intellij.psi.PsiElement
import java.awt.Font
import javax.swing.Icon
import motif.core.ResolvedGraph

/*
 * Node descriptor used to render a simple string, with no children descendant.
 */
class ScopeHierarchySimpleDescriptor(
    project: Project,
    graph: ResolvedGraph,
    parentDescriptor: HierarchyNodeDescriptor?,
    element: PsiElement,
    private val label: String,
) : ScopeHierarchyNodeDescriptor(project, graph, parentDescriptor, element, false) {

  override fun updateText(text: CompositeAppearance) {
    val textAttr = TextAttributes(myColor, null, null, null, Font.ITALIC)
    text.ending.addText(label, textAttr)
  }

  override fun getIcon(element: PsiElement): Icon? = null

  override fun toString(): String = label
}
