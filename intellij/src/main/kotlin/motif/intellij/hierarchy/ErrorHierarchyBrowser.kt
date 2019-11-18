/*
 * Copyright (c) 2018-2019 Uber Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http:www.apache.org/licenses/LICENSE-2.0
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
import com.intellij.ui.treeStructure.Tree
import motif.core.ResolvedGraph
import motif.errormessage.ErrorMessage
import motif.intellij.MotifProjectComponent
import motif.intellij.ScopeHierarchyUtils.Companion.isRootElement
import motif.intellij.hierarchy.ScopeHierarchyBrowser.Companion.LABEL_GO_NEXT_SCOPE
import motif.intellij.hierarchy.ScopeHierarchyBrowser.Companion.LABEL_GO_PREVIOUS_SCOPE
import motif.intellij.hierarchy.descriptor.ScopeHierarchyErrorDescriptor
import motif.intellij.hierarchy.descriptor.ScopeHierarchyRootErrorDescriptor
import motif.models.MotifError
import java.text.MessageFormat
import java.util.*
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode

/*
 * UI component used to render scope properties.
 */
class ErrorHierarchyBrowser(
        val project: Project,
        initialGraph: ResolvedGraph,
        private val rootElement: PsiElement,
        private val selectionListener: Listener?)
    : HierarchyBrowserBaseEx(project, rootElement), MotifProjectComponent.Listener {

    private var graph: ResolvedGraph = initialGraph

    companion object {
        const val ERROR_HIERARCHY_TYPE: String = "Errors"
        private val DATA_KEY = DataKey.create<ErrorHierarchyBrowser>(ErrorHierarchyBrowser::class.java.name)
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
        if (isRootElement(descriptor.psiElement)) {
            return null
        }
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
        trees[ERROR_HIERARCHY_TYPE] = createTree(true)
    }

    override fun configureTree(tree: Tree) {
        super.configureTree(tree)
        tree.addTreeSelectionListener {
          tree.addTreeSelectionListener {
                val node: Any? = tree.lastSelectedPathComponent
                if (node is DefaultMutableTreeNode) {
                    val descriptor = node.userObject
                    if (descriptor is ScopeHierarchyErrorDescriptor) {
                        selectionListener?.onSelectedErrorChanged(descriptor.element, descriptor.error, descriptor.errorMessage)
                    }
                }
            }
        }
    }

    override fun getContentDisplayName(typeName: String, element: PsiElement): String? {
        return MessageFormat.format(typeName, ClassPresentationUtil.getNameForClass(element as PsiClass, false))
    }

    override fun createHierarchyTreeStructure(typeName: String, psiElement: PsiElement): HierarchyTreeStructure? {
        if (psiElement == rootElement) {
            val descriptor: HierarchyNodeDescriptor = ScopeHierarchyRootErrorDescriptor(project, graph, null, psiElement)
            return ScopeHierarchyTreeStructure(project, graph, descriptor)
        }
        return null
    }

    override fun getBrowserDataKey(): String {
        return DATA_KEY.name
    }

    override fun doRefresh(currentBuilderOnly: Boolean) {
        MotifProjectComponent.getInstance(project).refreshGraph()
    }

    override fun onGraphUpdated(graph: ResolvedGraph) {
        this.graph = graph
        super.doRefresh(true)
    }

    /*
     * Interface used to notify that an new error was selected in the
     * {@ErrorHierarchyBrowser} component.
     */
    interface Listener {

        fun onSelectedErrorChanged(element: PsiElement, error: MotifError, errorMessage: ErrorMessage)
    }
}