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
package motif.intellij.hierarchy

import com.intellij.ide.hierarchy.HierarchyBrowserBaseEx
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor
import com.intellij.ide.hierarchy.HierarchyTreeStructure
import com.intellij.ide.hierarchy.JavaHierarchyUtil
import com.intellij.ide.util.treeView.NodeDescriptor
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.presentation.java.ClassPresentationUtil
import motif.core.ResolvedGraph
import motif.intellij.MotifProjectComponent
import motif.intellij.hierarchy.ScopeHierarchyBrowser.Companion.LABEL_GO_NEXT_SCOPE
import motif.intellij.hierarchy.ScopeHierarchyBrowser.Companion.LABEL_GO_PREVIOUS_SCOPE
import motif.intellij.hierarchy.descriptor.ScopeHierarchyUsageSectionDescriptor
import java.text.MessageFormat
import java.util.*
import javax.swing.JPanel
import javax.swing.JTree

/*
 * UI component used to render usage.
 */
class UsageHierarchyBrowser(
        val project: Project,
        initialGraph: ResolvedGraph,
        private val rootElement: PsiElement)
    : HierarchyBrowserBaseEx(project, rootElement), MotifProjectComponent.Listener {

    private var graph: ResolvedGraph = initialGraph

    companion object {
        const val USAGE_HIERARCHY_TYPE: String = "Usage"
        private val DATA_KEY = DataKey.create<UsageHierarchyBrowser>(UsageHierarchyBrowser::class.java.name)
    }

    fun setSelectedClass(element: PsiClass) {
        hierarchyBase = element
        super.doRefresh(true)
    }

    override fun isApplicableElement(element: PsiElement): Boolean {
        return element is PsiClass
    }

    override fun getActionPlace(): String {
        return ActionPlaces.METHOD_HIERARCHY_VIEW_TOOLBAR
    }

    override fun prependActions(actionGroup: DefaultActionGroup) {
    }

    override fun getComparator(): Comparator<NodeDescriptor<Any>>? {
        return JavaHierarchyUtil.getComparator(myProject)
    }

    override fun getElementFromDescriptor(descriptor: HierarchyNodeDescriptor): PsiElement? {
        return descriptor.psiElement
    }

    override fun getPrevOccurenceActionNameImpl(): String {
        return LABEL_GO_PREVIOUS_SCOPE
    }

    override fun getNextOccurenceActionNameImpl(): String {
        return LABEL_GO_NEXT_SCOPE
    }

    override fun createLegendPanel(): JPanel? {
        return null
    }

    override fun createTrees(trees: MutableMap<String, JTree>) {
        trees[USAGE_HIERARCHY_TYPE] = createTree(true)
    }

    override fun getContentDisplayName(typeName: String, element: PsiElement): String? {
        return MessageFormat.format(typeName, ClassPresentationUtil.getNameForClass(element as PsiClass, false))
    }

    override fun createHierarchyTreeStructure(typeName: String, psiElement: PsiElement): HierarchyTreeStructure? {
        if (psiElement is PsiClass) {
            val descriptor: HierarchyNodeDescriptor = ScopeHierarchyUsageSectionDescriptor(project, graph, null, psiElement)
            return ScopeHierarchyTreeStructure(project, graph, descriptor)
        }
        return null
    }

    override fun getBrowserDataKey(): String {
        return DATA_KEY.name
    }

    override fun onGraphUpdated(graph: ResolvedGraph) {
        this.graph = graph
        super.doRefresh(true)
    }
}