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
import java.util.ArrayDeque

class MotifCompleteTreeStructureUtility {

    /**
     * Creates the tree top down, starting from the top most parent, down to the scope at hand. This allows us to
     * pass the scope node to HierarchyTreeStructure class to build its children aka the lower tree using the Subtree
     * logic.
     */
    fun buildParentHierarchy(psiClass: PsiClass, project: Project): HierarchyNodeDescriptor {
        val component = MotifComponent.get(project)
        val scopeToParentsMap: Map<PsiClass, List<PsiClass>> = component.graphProcessor.scopeToParentsMap()
        var descriptor: HierarchyNodeDescriptor? = null
        val hierarchyRootAndAllNodes: HierarchyRootAndAllNodes = allNodesInHierarchy(psiClass, scopeToParentsMap)
        val scopeToChildrenMap: Map<PsiClass, List<PsiClass>> = component.graphProcessor.scopeToChildrenMap()
        val nodeQueue: ArrayDeque<PsiClass> = ArrayDeque(hierarchyRootAndAllNodes.allNodes.size)
        val classToDescriptorMap: MutableMap<PsiClass, HierarchyNodeDescriptor> = mutableMapOf()
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
