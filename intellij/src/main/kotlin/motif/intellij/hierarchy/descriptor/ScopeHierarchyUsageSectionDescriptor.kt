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
import com.intellij.psi.PsiClass
import motif.core.ResolvedGraph
import motif.intellij.ScopeHierarchyUtils.Companion.getUsageCount
import motif.intellij.ScopeHierarchyUtils.Companion.getUsageString

open class ScopeHierarchyUsageSectionDescriptor(
        private val nonNullproject: Project,
        graph: ResolvedGraph,
        parentDescriptor: HierarchyNodeDescriptor?,
        val clazz: PsiClass)
    : ScopeHierarchyNodeDescriptor(nonNullproject, graph, parentDescriptor, clazz, false) {

    override fun updateText(text: CompositeAppearance) {
        val count: Int = getUsageCount(nonNullproject, graph, clazz)
        text.ending.addText("Found usages")
        text.ending.addText(" " + getUsageString(count), getPackageNameAttributes())
    }
}