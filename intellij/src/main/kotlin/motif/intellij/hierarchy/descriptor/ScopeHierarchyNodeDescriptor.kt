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
import com.intellij.openapi.editor.markup.EffectType
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ui.util.CompositeAppearance
import com.intellij.openapi.util.Comparing
import com.intellij.psi.PsiElement
import java.awt.Color
import java.awt.Font
import motif.core.ResolvedGraph

/*
 * Base class for all tree node descriptors used in Motif IntelliJ plugin. It simplifies populate text content of
 * nodes, and provides an optional legend.
 */
open class ScopeHierarchyNodeDescriptor(
    project: Project,
    val graph: ResolvedGraph,
    val parentDescriptor: HierarchyNodeDescriptor?,
    val element: PsiElement,
    isBase: Boolean,
) : HierarchyNodeDescriptor(project, parentDescriptor, element, isBase) {

  open fun updateText(text: CompositeAppearance) {}

  open fun getLegend(): String? = null

  override fun update(): Boolean {
    val changes = super.update()

    if (psiElement == null) {
      return invalidElement()
    }

    val oldText = myHighlightedText
    myHighlightedText = CompositeAppearance()
    updateText(myHighlightedText)

    return changes || !Comparing.equal(myHighlightedText, oldText)
  }

  fun getDefaultTextAttributes(isError: Boolean = false): TextAttributes {
    val font: Int = if (myIsBase) Font.BOLD else Font.PLAIN
    return if (isError) {
      TextAttributes(myColor, null, Color.red, EffectType.WAVE_UNDERSCORE, font)
    } else {
      TextAttributes(myColor, null, null, null, font)
    }
  }
}
