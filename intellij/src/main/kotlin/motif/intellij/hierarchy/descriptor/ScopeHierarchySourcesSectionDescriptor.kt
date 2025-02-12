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
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ui.util.CompositeAppearance
import com.intellij.psi.PsiElement
import javax.swing.Icon
import motif.core.ResolvedGraph
import motif.intellij.ScopeHierarchyUtils
import motif.intellij.ScopeHierarchyUtils.getVisibleSources
import motif.models.Scope

open class ScopeHierarchySourcesSectionDescriptor(
    project: Project,
    graph: ResolvedGraph,
    parentDescriptor: HierarchyNodeDescriptor?,
    element: PsiElement,
    val scope: Scope,
    private val useLabel: Boolean = false,
) : ScopeHierarchyNodeDescriptor(project, graph, parentDescriptor, element, false) {

  override fun updateText(text: CompositeAppearance) {
    val label: String = if (useLabel) "Provides" else scope.simpleName
    val count: Int = getVisibleSources(graph, scope).count()
    text.ending.addText(label)
    text.ending.addText(
        " " + ScopeHierarchyUtils.getObjectString(count),
        getPackageNameAttributes(),
    )
  }

  override fun getIcon(element: PsiElement): Icon? = null
}
