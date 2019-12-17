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
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementFactory
import com.intellij.psi.PsiType
import com.intellij.psi.presentation.java.ClassPresentationUtil
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.tree.TreeUtil
import motif.ast.IrType
import motif.ast.intellij.IntelliJClass
import motif.ast.intellij.IntelliJType
import motif.core.ResolvedGraph
import motif.intellij.MotifProjectComponent
import motif.intellij.ScopeHierarchyUtils.Companion.isMotifScopeClass
import motif.intellij.hierarchy.ScopeHierarchyBrowser.Companion.LABEL_GO_NEXT_SCOPE
import motif.intellij.hierarchy.ScopeHierarchyBrowser.Companion.LABEL_GO_PREVIOUS_SCOPE
import motif.intellij.hierarchy.descriptor.*
import motif.models.Scope
import java.text.MessageFormat
import java.util.*
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode

/*
 * UI component used to render scope properties.
 */
class ScopePropertyHierarchyBrowser(
        val project: Project,
        initialGraph: ResolvedGraph,
        private val rootElement: PsiElement,
        private val hierarchyType: PropertyHierarchyType)
    : HierarchyBrowserBaseEx(project, rootElement), MotifProjectComponent.Listener {

    private var graph: ResolvedGraph = initialGraph

    enum class PropertyHierarchyType {
        CONSUME,
        PROVIDE,
        CONSUME_AND_PROVIDE,
        DEPENDENCIES
    }

    companion object {
        const val PROPERTY_HIERARCHY_TYPE: String = "Properties"
        private val DATA_KEY = DataKey.create<ScopePropertyHierarchyBrowser>(ScopePropertyHierarchyBrowser::class.java.name)
        private const val LABEL_NO_SCOPE: String = "No Scope is selected."
    }

    fun setSelectedScope(element: PsiElement) {
        hierarchyBase = element
        doRefresh(true)

        // Expand tree to show items under provides/consumes nodes
        if (hierarchyType == PropertyHierarchyType.CONSUME_AND_PROVIDE) {
            ApplicationManager.getApplication().invokeLater {
                TreeUtil.expand(currentTree, 2)
            }
        }
    }

    // HACK: prevent focus to be request when refresh is happening. This is to allow keyboard navigation in scope tree.
    override fun changeView(typeName: String) {
        super.changeView(typeName, false)
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

    override fun createTrees(trees: MutableMap<String, JTree>) {
        trees[PROPERTY_HIERARCHY_TYPE] = createTree(true)
    }

    override fun appendActions(actionGroup: DefaultActionGroup, helpID: String?) {
        val actionManager = ActionManager.getInstance()
        actionGroup.add(actionManager.getAction(IdeActions.ACTION_EXPAND_ALL))
    }

    override fun createLegendPanel(): JPanel? {
        return null
    }

    override fun configureTree(tree: Tree) {
        super.configureTree(tree)
        tree.addTreeSelectionListener {
            val node: Any? = tree.lastSelectedPathComponent
            if (node is DefaultMutableTreeNode) {
                val descriptor: ScopeHierarchyNodeDescriptor =
                        node.userObject as? ScopeHierarchyNodeDescriptor ?: return@addTreeSelectionListener
                val text: String? = descriptor.getLegend()
                // TODO : display legend in a popup or dialog
            }
        }
    }

    override fun getContentDisplayName(typeName: String, element: PsiElement): String? {
        return MessageFormat.format(typeName, ClassPresentationUtil.getNameForClass(element as PsiClass, false))
    }

    override fun createHierarchyTreeStructure(typeName: String, psiElement: PsiElement): HierarchyTreeStructure? {
        if (psiElement == rootElement) {
            val descriptor: HierarchyNodeDescriptor = ScopeHierarchySimpleDescriptor(project, graph, null, psiElement, LABEL_NO_SCOPE)
            return ScopeHierarchyTreeStructure(project, graph, descriptor)
        } else if (psiElement is PsiClass && isMotifScopeClass(psiElement)) {
            val scopeType: PsiType = PsiElementFactory.SERVICE.getInstance(project).createType(psiElement)
            val type: IrType = IntelliJType(project, scopeType)
            val scope: Scope = graph.getScope(type) ?: return null
            return when (hierarchyType) {
                PropertyHierarchyType.CONSUME -> {
                    val descriptor: HierarchyNodeDescriptor = ScopeHierarchySinksSectionDescriptor(project, graph, null, (scope.clazz as IntelliJClass).psiClass, scope)
                    ScopeHierarchyTreeStructure(project, graph, descriptor)
                }
                PropertyHierarchyType.PROVIDE -> {
                    val descriptor: HierarchyNodeDescriptor = ScopeHierarchySourcesSectionDescriptor(project, graph, null, (scope.clazz as IntelliJClass).psiClass, scope)
                    ScopeHierarchyTreeStructure(project, graph, descriptor)
                }
                PropertyHierarchyType.CONSUME_AND_PROVIDE -> {
                    val descriptor: HierarchyNodeDescriptor = ScopeHierarchySourcesAndSinksSectionDescriptor(project, graph, null, (scope.clazz as IntelliJClass).psiClass, scope)
                    ScopeHierarchyTreeStructure(project, graph, descriptor)
                }
                PropertyHierarchyType.DEPENDENCIES -> {
                    val descriptor: HierarchyNodeDescriptor = ScopeHierarchyDependenciesSectionDescriptor(project, graph, null, (scope.clazz as IntelliJClass).psiClass, scope)
                    ScopeHierarchyTreeStructure(project, graph, descriptor)
                }
            }
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