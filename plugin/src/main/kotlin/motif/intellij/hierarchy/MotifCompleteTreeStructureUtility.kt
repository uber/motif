/*
 * Copyright (c) 2018 Uber Technologies, Inc.
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

import com.intellij.ide.hierarchy.HierarchyNodeDescriptor
import com.intellij.ide.hierarchy.type.TypeHierarchyNodeDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import motif.intellij.MotifComponent
import motif.intellij.graph.GraphProcessor
import java.util.ArrayDeque

/**
 * Creates a complete upward graph of Motif Scopes for a given PsiClass.
 */
class MotifCompleteTreeStructureUtility {

    /**
     * Using the list of all nodes for building the hierarchy, and the root from the HierarchyRootAndAllNodes object
     * computed by the allNodesInHierarchy method, this builds back the graph in HierarchyNodeDescriptor objects,
     * with the original PsiClass as the final child / leaf node.
     */
    fun buildParentHierarchy(psiClass: PsiClass, project: Project): HierarchyNodeDescriptor {
        // get information about parent <-> child scope relationships from the GraphProcessor, and create the
        // HierarchyRootAndAllNodes object
        val graphProcessor: GraphProcessor = MotifComponent.get(project).graphProcessor
        val scopeToParentsMap: Map<PsiClass, List<PsiClass>> = graphProcessor.scopeToParentsMap()
        val scopeToChildrenMap: Map<PsiClass, List<PsiClass>> = graphProcessor.scopeToChildrenMap()
        val hierarchyRootAndAllNodes: HierarchyRootAndAllNodes = allNodesInHierarchy(psiClass, scopeToParentsMap)

        // setup to start building the actual graph
        var descriptor: HierarchyNodeDescriptor? = null
        val nodeQueue: ArrayDeque<PsiClass> = ArrayDeque(hierarchyRootAndAllNodes.allNodes.size)
        val classToDescriptorMap: MutableMap<PsiClass, HierarchyNodeDescriptor> = mutableMapOf()

        // start from the root, and run BFS over all children in the list of nodes from HierarchyRootAndAllNodes.
        // This allows us to build a complete graph of all nodes in the upwards hierarchy of the base PsiClass.
        nodeQueue.add(hierarchyRootAndAllNodes.root)
        while (nodeQueue.isNotEmpty()) {
            val currNode = nodeQueue.pop()
            var newDescriptor: HierarchyNodeDescriptor? = classToDescriptorMap[currNode]
            if (newDescriptor == null) {
                newDescriptor = TypeHierarchyNodeDescriptor(project, descriptor, currNode, false)
                classToDescriptorMap[currNode] = newDescriptor
            }
            val childList = scopeToChildrenMap[currNode]?.filter { hierarchyRootAndAllNodes.allNodes.contains(it) }
            if (childList == null || childList.isEmpty()) {
                descriptor = classToDescriptorMap[currNode]
                break
            }
            val childDescriptorArray: MutableList<HierarchyNodeDescriptor> = mutableListOf()
            childList.forEach {
                nodeQueue.add(it)
                if (!classToDescriptorMap.containsKey(it)) {
                    val childDescriptor = TypeHierarchyNodeDescriptor(project, newDescriptor, it, false)
                    childDescriptorArray.add(childDescriptor)
                    classToDescriptorMap[it] = childDescriptor
                } else {
                    childDescriptorArray.add(classToDescriptorMap[it]!!)
                }
            }
            newDescriptor.cachedChildren = childDescriptorArray.toTypedArray()
        }
        return descriptor!!
    }

    /**
     * Starts from the provided psiClass and performs BFS up the tree, exploring the entire parent hierarchy to the
     * root. This assumes that all scopes eventually end up at the same root. Returns a HierarchyRootAndAllNodes
     * object, containing the source of the entire graph, along with a list of all nodes explored i.e. all nodes in
     * all paths, from the PsiClass to the source.
     */
    private fun allNodesInHierarchy(psiClass: PsiClass, scopeToParentsMap: Map<PsiClass, List<PsiClass>>): HierarchyRootAndAllNodes {
        var root: PsiClass = psiClass
        val visitedNodes: MutableSet<PsiClass> = mutableSetOf()
        val nodeQueue: ArrayDeque<PsiClass> = ArrayDeque(20)
        nodeQueue.add(psiClass)
        while (nodeQueue.isNotEmpty()) {
            val currNode = nodeQueue.pop()
            visitedNodes.add(currNode)
            val tempParentList = scopeToParentsMap[currNode]
            if (tempParentList == null || tempParentList.isEmpty()) {
                root = currNode
            } else {
                nodeQueue.addAll(tempParentList)
            }
        }
        return HierarchyRootAndAllNodes(root, visitedNodes)
    }

    private data class HierarchyRootAndAllNodes(val root: PsiClass, val allNodes: Set<PsiClass>)
}
