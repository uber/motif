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
package motif.intellij

import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.TreeStructureProvider
import com.intellij.ide.util.treeView.AbstractTreeBuilder
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.ide.util.treeView.AbstractTreeStructureBase
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.Label
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.update.MergingUpdateQueue
import com.intellij.util.ui.update.Update
import motif.core.ResolvedGraph
import motif.errormessage.ErrorMessage
import motif.models.FactoryMethod
import motif.models.Scope
import java.awt.Component
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.LinkedBlockingQueue
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeCellRenderer

class MotifPanel(
        private val project: Project
) : SimpleToolWindowPanel(true, true) {

    private val graphFactory = GraphFactory(project)
    private val mergingUpdateQueue = MergingUpdateQueue(
            "motif-panel",
            100,
            true,
            null,
            null,
            null,
            false)
    private val changedElements = LinkedBlockingQueue<PsiElement>()

    private val rootNode = MotifRootNode(project)
    private val model = DefaultTreeModel(DefaultMutableTreeNode().apply { userObject = rootNode })
    private val tree = Tree(model).apply { cellRenderer = MotifCellRenderer(project) }
    private val structure = MotifTreeStructure(project, rootNode)
    private val builder = MotifTreeBuilder(tree, model, structure)
    private val scrollPane = JBScrollPane(tree)

    private var graphInvalidator: GraphInvalidator? = null

    init {
        val refreshAction = object : AnAction("Refresh", "Refresh", AllIcons.Actions.Refresh) {

            override fun actionPerformed(e: AnActionEvent) {
                refresh()
            }
        }
        val actionToolbar = ActionManager.getInstance()
                .createActionToolbar("Motif Panel Actions", DefaultActionGroup(refreshAction), true)
        setContent(scrollPane)
        setToolbar(actionToolbar.component)
        PsiManager.getInstance(project).addPsiTreeChangeListener(ChildChangeListener { element ->
            changedElements.add(element)
            mergingUpdateQueue.queue(object : Update("check-graph", false) {

                override fun run() {
                    val graphInvalidator = graphInvalidator ?: return
                    ApplicationManager.getApplication().runReadAction {
                        val changedElements = mutableListOf<PsiElement>().apply { changedElements.drainTo(this) }
                        if (changedElements.any(graphInvalidator::shouldInvalidate)) {
                            log("INVALID")
                        } else {
                            log("OK")
                        }
                    }
                }
            })
        })
    }

    private fun refresh() {
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Refresh") {

            override fun run(indicator: ProgressIndicator) {
                ApplicationManager.getApplication().runReadAction {
                    val graph = graphFactory.compute()
                    graphInvalidator = GraphInvalidator(project, graph)
                    rootNode.graph = graph
                    ApplicationManager.getApplication().invokeLater {
                        builder.queueUpdate()
                    }
                }
            }
        })
    }
}

class MotifTreeBuilder(
        tree: Tree,
        model: DefaultTreeModel,
        treeStructure: MotifTreeStructure
) : AbstractTreeBuilder(tree, model, treeStructure, null, false)

class MotifTreeStructure(
        private val project: Project,
        private val rootNode: MotifRootNode
) : AbstractTreeStructureBase(project) {

    override fun getRootElement() = rootNode

    override fun commit() = PsiDocumentManager.getInstance(myProject).commitAllDocuments()

    override fun getProviders() = emptyList<TreeStructureProvider>()

    override fun hasSomethingToCommit() = PsiDocumentManager.getInstance(myProject).hasUncommitedDocuments();
}

class MotifRootNode(project: Project) : AbstractTreeNode<String>(project, "root") {

    var graph: ResolvedGraph? = null

    override fun update(presentation: PresentationData) {}

    override fun getChildren(): List<ScopeNode> {
        val graph = this.graph ?: return emptyList()
        if (graph.errors.isNotEmpty()) {
            log(ErrorMessage.toString(graph))
        }
        return graph.roots.map { ScopeNode(project!!, graph, it) }
    }
}

class ScopeNode(
        private val project_: Project,
        private val graph: ResolvedGraph,
        private val scope: Scope) : AbstractTreeNode<String>(project_, scope.simpleName) {

    override fun update(presentation: PresentationData) {}

    override fun getChildren(): List<AbstractTreeNode<*>> {
        return getChildNodes() + getFactoryMethodNodes()
    }

    private fun getChildNodes(): List<ScopeNode> {
        return graph.getChildEdges(scope).map { childEdge ->
            val childScope = childEdge.child
            ScopeNode(project!!, graph, childScope)
        }
    }

    private fun getFactoryMethodNodes(): List<FactoryMethodNode> {
        return scope.factoryMethods.map { FactoryMethodNode(project_, graph, it) }
    }
}

class FactoryMethodNode(
        project: Project,
        private val graph: ResolvedGraph,
        private val factoryMethod: FactoryMethod) : AbstractTreeNode<String>(project, factoryMethod.name) {

    override fun update(presentation: PresentationData) {}

    override fun getChildren(): List<AbstractTreeNode<Any>> = emptyList()
}

class MotifCellRenderer(private val project: Project) : TreeCellRenderer {

    override fun getTreeCellRendererComponent(tree: JTree, value: Any, selected: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): Component {
        val userObject = (value as DefaultMutableTreeNode).userObject
        return when (userObject) {
            is String -> Label(userObject)
            is AbstractTreeNode<*> -> Label(userObject.value.toString())
            else -> throw IllegalStateException()
        }
    }
}

class MotifPanelFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val content = toolWindow.contentManager.factory.createContent(MotifPanel(project), "Motif", false)
        toolWindow.contentManager.addContent(content)
    }
}

fun log(o: Any?) {
    Notifications.Bus.notify(
            Notification("Motif", "Motif Tree", o.toString(), NotificationType.INFORMATION))
}
