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

import com.intellij.ide.hierarchy.HierarchyNodeDescriptor
import com.intellij.ide.hierarchy.HierarchyTreeStructure
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementFactory
import com.intellij.psi.PsiType
import motif.ast.IrType
import motif.ast.intellij.IntelliJClass
import motif.ast.intellij.IntelliJMethod
import motif.ast.intellij.IntelliJType
import motif.core.ResolvedGraph
import motif.errormessage.ErrorMessage
import motif.intellij.ScopeHierarchyUtils
import motif.intellij.hierarchy.descriptor.*
import motif.models.*

class ScopeHierarchyTreeStructure(val project: Project, val graph: ResolvedGraph, descriptor: HierarchyNodeDescriptor)
    : HierarchyTreeStructure(project, descriptor) {

    companion object {
        const val LABEL_SCOPE_NO_PROVIDE: String = "This Scope does not provide any object."
        const val LABEL_SCOPE_NO_CONSUME: String = "This Scope does not consume any object."
        const val LABEL_SCOPE_NO_DEPENDENCIES: String = "This Scope does not have any dependencies."
    }

    init {
        setBaseElement(descriptor)
    }

    override fun buildChildren(descriptor: HierarchyNodeDescriptor): Array<Any> {
        val descriptors: ArrayList<HierarchyNodeDescriptor> = ArrayList(1)
        when (descriptor) {
            is ScopeHierarchyRootDescriptor -> {
                graph.roots.forEach { scope ->
                    descriptors.add(ScopeHierarchyScopeDescriptor(myProject, graph, descriptor, (scope.clazz as IntelliJClass).psiClass, scope, false))
                }
            }
            is ScopeHierarchyScopeDescriptor -> {
                graph.getChildEdges(descriptor.scope).forEach { edge ->
                    descriptors.add(ScopeHierarchyScopeDescriptor(myProject, graph, descriptor, (edge.child.clazz as IntelliJClass).psiClass, edge.child, false))
                }
            }
            is ScopeHierarchySourcesSectionDescriptor -> {
                graph.getSources(descriptor.scope).forEach { source ->
                    descriptors.add(ScopeHierarchySourceDescriptor(myProject, graph, descriptor, source))
                }
                if (descriptors.isEmpty()) {
                    descriptors.add(ScopeHierarchySimpleDescriptor(myProject, graph, descriptor, descriptor.element, LABEL_SCOPE_NO_PROVIDE))
                }
            }
            is ScopeHierarchySinksSectionDescriptor -> {
                graph.getSinks(descriptor.scope).forEach { sink ->
                    descriptors.add(ScopeHierarchySinkDescriptor(myProject, graph, descriptor, sink))
                }
                if (descriptors.isEmpty()) {
                    descriptors.add(ScopeHierarchySimpleDescriptor(myProject, graph, descriptor, descriptor.element, LABEL_SCOPE_NO_CONSUME))
                }
            }
            is ScopeHierarchyDependenciesSectionDescriptor -> {
                val dependencies: Dependencies? = descriptor.scope.dependencies
                if (dependencies != null && dependencies.methods.isNotEmpty()) {
                    dependencies.methods.forEach { method ->
                        descriptors.add(ScopeHierarchyDependencyDescriptor(myProject, graph, descriptor, (method.method as IntelliJMethod).psiMethod, method))
                    }
                }
                if (descriptors.isEmpty()) {
                    descriptors.add(ScopeHierarchySimpleDescriptor(myProject, graph, descriptor, descriptor.psiElement!!, LABEL_SCOPE_NO_DEPENDENCIES))
                }
            }
            is ScopeHierarchySinkDetailsDescriptor -> {
                // returns no children
            }
            is ScopeHierarchySourceDetailsDescriptor -> {
                // returns no children
            }
            is ScopeHierarchySinkDescriptor -> {
                graph.getProviders(descriptor.sink).forEach { source ->
                    descriptors.add(ScopeHierarchySourceDetailsDescriptor(myProject, graph, descriptor, source))
                }
            }
            is ScopeHierarchySourceDescriptor -> {
                graph.getConsumers(descriptor.source).forEach { sink ->
                    descriptors.add(ScopeHierarchySinkDetailsDescriptor(myProject, graph, descriptor, sink))
                }
            }
            is ScopeHierarchyRootErrorDescriptor -> {
                graph.errors.forEach { error ->
                    val errorMessage: ErrorMessage = ErrorMessage.get(graph, error)
                    descriptors.add(ScopeHierarchyErrorDescriptor(myProject, graph, descriptor, error, errorMessage))
                }
            }
            is ScopeHierarchyUsageSectionDescriptor -> {
                val countSources: Int = ScopeHierarchyUtils.getUsageCount(project, graph, descriptor.clazz, includeSources = true, includeSinks = false)
                val countSinks: Int = ScopeHierarchyUtils.getUsageCount(project, graph, descriptor.clazz, includeSources = false, includeSinks = true)
                if (countSources > 0) {
                    descriptors.add(ScopeHierarchyUsageSourcesSectionDescriptor(myProject, graph, descriptor, descriptor.clazz))
                }
                if (countSinks > 0) {
                    descriptors.add(ScopeHierarchyUsageSinksSectionDescriptor(myProject, graph, descriptor, descriptor.clazz))
                }
            }
            is ScopeHierarchyUsageSourcesSectionDescriptor -> {
                val elementType: PsiType = PsiElementFactory.SERVICE.getInstance(project).createType(descriptor.clazz)
                val type: IrType = IntelliJType(project, elementType)
                graph.getSources(type).forEach { source ->
                    descriptors.add(ScopeHierarchySourceDetailsDescriptor(myProject, graph, descriptor, source))
                }
            }
            is ScopeHierarchyUsageSinksSectionDescriptor -> {
                val elementType: PsiType = PsiElementFactory.SERVICE.getInstance(project).createType(descriptor.clazz)
                val type: IrType = IntelliJType(project, elementType)
                graph.getSinks(type).forEach { sink ->
                    descriptors.add(ScopeHierarchySinkDetailsDescriptor(myProject, graph, descriptor, sink))
                }
            }
        }
        return descriptors.toTypedArray()
    }
}