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
package motif.intellij.hierarchy.ui

import com.intellij.ide.hierarchy.HierarchyNodeDescriptor
import com.intellij.ide.hierarchy.HierarchyTreeStructure
import com.intellij.ide.hierarchy.JavaHierarchyUtil
import com.intellij.ide.hierarchy.TypeHierarchyBrowserBase
import com.intellij.ide.hierarchy.type.TypeHierarchyNodeDescriptor
import com.intellij.ide.util.treeView.NodeDescriptor
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import motif.intellij.isScopeClass
import java.util.Comparator
import javax.swing.JPanel
import javax.swing.JTree

class MotifScopeHierarchyBrowser(project: Project?, element: PsiElement?) : TypeHierarchyBrowserBase(project, element) {

    override fun getQualifiedName(psiElement: PsiElement?): String {
        return if (psiElement is PsiClass) psiElement.qualifiedName!! else ""
    }

    override fun isApplicableElement(element: PsiElement): Boolean {
        return element is PsiClass && element.isScopeClass()
    }

    override fun isInterface(psiElement: PsiElement): Boolean {
        return false
    }

    override fun getComparator(): Comparator<NodeDescriptor<Any>>? {
        return JavaHierarchyUtil.getComparator(myProject)
    }

    override fun getElementFromDescriptor(descriptor: HierarchyNodeDescriptor): PsiElement? {
        return (descriptor as? TypeHierarchyNodeDescriptor)?.psiClass
    }

    override fun createLegendPanel(): JPanel? {
        return null
    }

    override fun createTrees(trees: MutableMap<String, JTree>) {
        createTreeAndSetupCommonActions(trees, IdeActions.GROUP_TYPE_HIERARCHY_POPUP)
    }

    override fun createHierarchyTreeStructure(typeName: String, psiElement: PsiElement): HierarchyTreeStructure? {
        if (typeName == TypeHierarchyBrowserBase.TYPE_HIERARCHY_TYPE) {
               val completeTreeStructure = MotifCompleteTreeStructureUtility()
               val descriptor: HierarchyNodeDescriptor = completeTreeStructure.buildParentHierarchy(psiElement as
                PsiClass, myProject)
                return MotifScopeHierarchyTreeStructure(myProject, descriptor)
            }
        return MotifScopeHierarchyTreeStructure(myProject, psiElement as PsiClass, typeName)
    }

    override fun canBeDeleted(psiElement: PsiElement?): Boolean {
        return false
    }
}