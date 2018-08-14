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
import com.intellij.ide.hierarchy.HierarchyTreeStructure
import com.intellij.ide.hierarchy.TypeHierarchyBrowserBase
import com.intellij.ide.hierarchy.type.TypeHierarchyNodeDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import motif.intellij.MotifComponent
import motif.intellij.psi.isScopeClass

open class MotifScopeHierarchyTreeStructure: HierarchyTreeStructure {

    private val scopeToParentsMap: MutableMap<PsiClass, List<PsiClass>> = mutableMapOf()
    private val scopeToChildrenMap: MutableMap<PsiClass, List<PsiClass>> = mutableMapOf()
    private var project: Project
    private var hierarchyType: String = TypeHierarchyBrowserBase.TYPE_HIERARCHY_TYPE

    constructor(project: Project, descriptor: HierarchyNodeDescriptor) : super(project, descriptor) {
        hierarchyType = TypeHierarchyBrowserBase.TYPE_HIERARCHY_TYPE
        this.project = project
        setBaseElement(myBaseDescriptor)
        setup()
    }

    constructor(project: Project, aClass: PsiClass, typeName: String): super(project, TypeHierarchyNodeDescriptor(project, null, aClass, true)) {
        hierarchyType = typeName
        this.project = project
        setup()
    }

    private fun setup() {
        val component = MotifComponent.get(project)
        val graphProcessor = component.graphProcessor
        scopeToParentsMap.putAll(graphProcessor.scopeToParentsMap())
        scopeToChildrenMap.putAll(graphProcessor.scopeToChildrenMap())
    }

    override fun buildChildren(descriptor: HierarchyNodeDescriptor): Array<Any> {
        val typeDescriptor: TypeHierarchyNodeDescriptor = descriptor as TypeHierarchyNodeDescriptor
        val element: PsiClass = typeDescriptor.psiClass as? PsiClass ?: return emptyArray()
        val descriptors: MutableList<HierarchyNodeDescriptor> = mutableListOf()
        when(hierarchyType) {
            TypeHierarchyBrowserBase.SUBTYPES_HIERARCHY_TYPE, TypeHierarchyBrowserBase.TYPE_HIERARCHY_TYPE -> {
                getChildrenIfAny(element).forEach {
                    descriptors.add(TypeHierarchyNodeDescriptor(myProject, descriptor, it, false))
                }
            }
            TypeHierarchyBrowserBase.SUPERTYPES_HIERARCHY_TYPE -> {
                getParentsIfAny(element).forEach {
                    descriptors.add(TypeHierarchyNodeDescriptor(myProject, descriptor, it, false))
                }
            }
        }
        return descriptors.toTypedArray()
    }

    private fun getParentsIfAny(psiClass: PsiClass): List<PsiClass> {
        if (!psiClass.isScopeClass()) return emptyList()
        return scopeToParentsMap[psiClass] ?: emptyList()
    }

    private fun getChildrenIfAny(psiClass: PsiClass): List<PsiClass> {
        if (!psiClass.isScopeClass()) return emptyList()
        return scopeToChildrenMap[psiClass] ?: emptyList()
    }
}
