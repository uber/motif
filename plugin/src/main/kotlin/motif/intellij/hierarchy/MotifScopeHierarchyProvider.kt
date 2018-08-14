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

import com.intellij.codeInsight.TargetElementUtil
import com.intellij.ide.hierarchy.HierarchyBrowser
import com.intellij.ide.hierarchy.HierarchyProvider
import com.intellij.ide.hierarchy.TypeHierarchyBrowserBase
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.psi.*
import motif.intellij.psi.isScopeClass

/**
 * Motif's custom Hierarchy Provider to replace Java's JavaTypeHierarchyProvider when looking at Motif Scopes.
 */
class MotifScopeHierarchyProvider : HierarchyProvider {

    /**
     * Dictates that the default view to display is the entire hierarchy for the selected Motif Scope.
     */
    override fun browserActivated(hierarchyBrowser: HierarchyBrowser) {
        val browser: MotifScopeHierarchyBrowser = hierarchyBrowser as MotifScopeHierarchyBrowser
        browser.changeView(TypeHierarchyBrowserBase.TYPE_HIERARCHY_TYPE)
    }

    /**
     * Returns the custom MotifScopeHierarchyBrowser instead of the default JavaTypeHierarchyBrowser.
     */
    override fun createHierarchyBrowser(target: PsiElement): HierarchyBrowser {
        return MotifScopeHierarchyBrowser(target.project, target as PsiClass)
    }

    /**
     * Determines if the target element is a Motif Scope. If so, it returns the PsiElement corresponding to it,
     * otherwise, returns null. Returning null invalidates this provider, making IntelliJ move on to the next
     * provider, which is usually the JavaTypeHierarchyProvider.
     */
    override fun getTarget(dataContext: DataContext): PsiElement? {

        val editor = CommonDataKeys.EDITOR.getData(dataContext) ?: return null

        val targetElement = TargetElementUtil.findTargetElement(editor, TargetElementUtil.ELEMENT_NAME_ACCEPTED or
                    TargetElementUtil.REFERENCED_ELEMENT_ACCEPTED or TargetElementUtil.LOOKUP_ITEM_ACCEPTED)

        return if (targetElement is PsiClass && targetElement.isScopeClass()) targetElement else null
    }

    companion object {
        val INSTANCE : MotifScopeHierarchyProvider = MotifScopeHierarchyProvider()
    }
}