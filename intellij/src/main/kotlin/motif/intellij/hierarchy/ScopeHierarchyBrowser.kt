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

import com.intellij.icons.AllIcons
import com.intellij.ide.BrowserUtil
import com.intellij.ide.IdeBundle
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor
import com.intellij.ide.hierarchy.HierarchyTreeStructure
import com.intellij.ide.hierarchy.JavaHierarchyUtil
import com.intellij.ide.util.treeView.NodeDescriptor
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementFactory
import com.intellij.psi.PsiType
import com.intellij.psi.presentation.java.ClassPresentationUtil
import com.intellij.ui.treeStructure.Tree
import motif.ast.IrType
import motif.ast.intellij.IntelliJClass
import motif.ast.intellij.IntelliJType
import motif.core.ResolvedGraph
import motif.intellij.MotifProjectComponent
import motif.intellij.ScopeHierarchyUtils
import motif.intellij.ScopeHierarchyUtils.Companion.isMotifScopeClass
import motif.intellij.analytics.AnalyticsProjectComponent
import motif.intellij.analytics.MotifAnalyticsActions
import motif.intellij.hierarchy.descriptor.ScopeHierarchyRootDescriptor
import motif.intellij.hierarchy.descriptor.ScopeHierarchyScopeAncestorDescriptor
import motif.intellij.hierarchy.descriptor.ScopeHierarchyScopeDescriptor
import motif.models.Scope
import java.text.MessageFormat
import java.util.*
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode

/*
 * UI component used to render tree of Motif scopes.
 */
class ScopeHierarchyBrowser(
        project: Project,
        initialGraph: ResolvedGraph,
        private val rootElement: PsiElement,
        private val selectionListener: Listener?)
    : HierarchyBrowserBase(project, rootElement), MotifProjectComponent.Listener {

    companion object {
        const val LABEL_GO_PREVIOUS_SCOPE: String = "Go to previous Scope."
        const val LABEL_GO_NEXT_SCOPE: String = "Go to next Scope"
        const val TYPE_HIERARCHY_TYPE: String = "Scopes"
        private val DATA_KEY = DataKey.create<ScopeHierarchyBrowser>(ScopeHierarchyBrowser::class.java.name)
    }

    enum class Status {
        UNINITIALIZED,
        INITIALIZING,
        INITIALIZED,
        REFRESHING,
    }

    private var status: Status = Status.UNINITIALIZED
    private var graph: ResolvedGraph = initialGraph

    fun setSelectedScope(element: PsiElement) {
        hierarchyBase = element
        super.doRefresh(true)
    }

    fun isUpdating(): Boolean {
        return status == Status.INITIALIZING || status == Status.REFRESHING
    }

    override fun isApplicableElement(element: PsiElement): Boolean {
        return element is PsiClass
    }

    override fun getActionPlace(): String {
        return ActionPlaces.METHOD_HIERARCHY_VIEW_TOOLBAR
    }

    override fun getComparator(): Comparator<NodeDescriptor<out Any>> {
        return JavaHierarchyUtil.getComparator(myProject)
    }

    override fun getElementFromDescriptor(descriptor: HierarchyNodeDescriptor): PsiElement? {
        if (ScopeHierarchyUtils.isRootElement(descriptor.psiElement)) {
            return null
        }
        return descriptor.psiElement
    }

    override fun getPrevOccurenceActionNameImpl(): String {
        return LABEL_GO_PREVIOUS_SCOPE
    }

    override fun createLegendPanel(): JPanel? {
        return null
    }

    override fun createTrees(trees: MutableMap<String, JTree>) {
        trees[TYPE_HIERARCHY_TYPE] = createTree(true)
    }

    override fun getNextOccurenceActionNameImpl(): String {
        return LABEL_GO_NEXT_SCOPE
    }

    override fun getContentDisplayName(typeName: String, element: PsiElement): String? {
        if (element !is PsiClass) {
            return null
        }
        return MessageFormat.format(typeName, ClassPresentationUtil.getNameForClass(element, false))
    }

    override fun appendActions(actionGroup: DefaultActionGroup, helpID: String?) {
        super.appendActions(actionGroup, helpID)

        // replace original refresh action with custom one, so that we can gray it out
        actionGroup.replaceAction(actionGroup.getChildren(null).first(), RefreshAction())

        // add help button and file an issue button
        actionGroup.add(FileAnIssueAction())
        actionGroup.add(HelpAction())
    }

    override fun createHierarchyTreeStructure(typeName: String, psiElement: PsiElement): HierarchyTreeStructure? {
        if (psiElement == rootElement) {
            // Display entire graph hierarchy
            return ScopeHierarchyTreeStructure(
                    myProject,
                    graph,
                    ScopeHierarchyRootDescriptor(myProject, graph, psiElement, status))
        } else if (psiElement is PsiClass && isMotifScopeClass(psiElement)) {
            // Display the scope ancestors hierarchy
            val scopeType: PsiType = PsiElementFactory.SERVICE.getInstance(project).createType(psiElement)
            val type: IrType = IntelliJType(project, scopeType)
            graph.getScope(type)?.let { scope ->
                val clazz = (scope.clazz as IntelliJClass).psiClass
                val descriptor = ScopeHierarchyScopeAncestorDescriptor(myProject, graph, null, clazz, scope, true)
                return ScopeHierarchyTreeStructure(myProject, graph, descriptor)
            }
        }
        return null
    }

    override fun getBrowserDataKey(): String {
        return DATA_KEY.name
    }

    override fun configureTree(tree: Tree) {
        super.configureTree(tree)
        tree.addTreeSelectionListener {
            val node: Any? = tree.lastSelectedPathComponent
            if (node is DefaultMutableTreeNode) {
                val descriptor = node.userObject
                if (descriptor is ScopeHierarchyScopeDescriptor) {
                    selectionListener?.onSelectedScopeChanged(descriptor.element, descriptor.scope)
                }
            }
        }
    }

    override fun doRefresh(currentBuilderOnly: Boolean) {
        when (status) {
            Status.INITIALIZED -> {
                status = Status.REFRESHING
            }
            Status.UNINITIALIZED -> {
                status = Status.INITIALIZING
                refresh()
            }
        }

        MotifProjectComponent.getInstance(project).refreshGraph()

        val action: String = if (status == Status.INITIALIZING) MotifAnalyticsActions.GRAPH_INIT else MotifAnalyticsActions.GRAPH_UPDATE
        AnalyticsProjectComponent.getInstance(project).logEvent(action)
    }

    /*
     * Request view to refresh, which causes most recent Motif graph to be used.
     */
    private fun refresh() {
        super.doRefresh(true)
    }

    override fun onGraphUpdated(graph: ResolvedGraph) {
        this.status = Status.INITIALIZED
        this.graph = graph
        refresh()
    }

    private inner class RefreshAction internal constructor() : com.intellij.ide.actions.RefreshAction(IdeBundle.message("action.refresh"), IdeBundle.message("action.refresh"), AllIcons.Actions.Refresh) {

        override fun actionPerformed(e: AnActionEvent) {
            doRefresh(false)
        }

        override fun update(event: AnActionEvent) {
            event.presentation.isEnabled = !isUpdating()
        }
    }

    private inner class HelpAction internal constructor() : AnAction(IdeBundle.message("action.help"), IdeBundle.message("action.help"), AllIcons.General.TodoQuestion) {

        override fun actionPerformed(e: AnActionEvent) {
            BrowserUtil.open("https://github.com/uber/motif/wiki/Motif-IntelliJ-IDE-Plugin-Help");
        }

        override fun update(event: AnActionEvent) {
            event.presentation.isEnabled = true
        }
    }

    private inner class FileAnIssueAction internal constructor() : AnAction("File an issue", "File an issue or feature request", AllIcons.Toolwindows.ToolWindowDebugger) {

        override fun actionPerformed(e: AnActionEvent) {
            BrowserUtil.open("https://github.com/uber/motif/issues/new?title=[Motif%20IDE%20Plugin]%20:%20%3Center%20issue%20title%20here%3E");
        }

        override fun update(event: AnActionEvent) {
            event.presentation.isEnabled = true
        }
    }

    /*
     * Interface used to notify that an new element was selected in
     * {@ScopeHierarchyBrowser} component.
     */
    interface Listener {
        fun onSelectedScopeChanged(element: PsiElement, scope: Scope)
    }
}