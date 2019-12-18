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
import motif.core.ResolvedGraph
import motif.intellij.ScopeHierarchyUtils.Companion.formatQualifiedName
import motif.models.Scope
import java.awt.Font.BOLD

open class ScopeHierarchySourcesAndSinksSectionDescriptor(
        project: Project,
        graph: ResolvedGraph,
        parentDescriptor: HierarchyNodeDescriptor?,
        element: PsiElement,
        val scope: Scope)
    : ScopeHierarchyNodeDescriptor(project, graph, parentDescriptor, element, false) {

    override fun updateText(text: CompositeAppearance) {
        text.ending.addText(scope.simpleName, TextAttributes(myColor, null, null, null, BOLD))
        text.ending.addText(" (" + formatQualifiedName(scope.qualifiedName) + ")", getPackageNameAttributes())
    }
}

