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

import com.intellij.icons.AllIcons
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ui.util.CompositeAppearance
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import javax.swing.Icon
import motif.core.ResolvedGraph
import motif.intellij.ScopeHierarchyUtils.formatQualifiedName
import motif.models.ErrorScope
import motif.models.Scope

/*
 * Node descriptor used to render a Motif scope.
 */
open class ScopeHierarchyScopeDescriptor(
    project: Project,
    graph: ResolvedGraph,
    parentDescriptor: HierarchyNodeDescriptor?,
    private val clazz: PsiClass,
    val scope: Scope,
    isBase: Boolean = false,
) : ScopeHierarchyNodeDescriptor(project, graph, parentDescriptor, clazz, isBase) {

  override fun updateText(text: CompositeAppearance) {
    val textAttr: TextAttributes = getDefaultTextAttributes(scope is ErrorScope)
    text.ending.addText(clazz.name, textAttr)
    val qualifiedName: String? = clazz.qualifiedName
    qualifiedName?.let {
      text.ending.addText(" (" + formatQualifiedName(it) + ")", getPackageNameAttributes())
    }
  }

  override fun getIcon(element: PsiElement): Icon? = AllIcons.Nodes.Interface

  override fun toString(): String = clazz.name ?: ""
}
