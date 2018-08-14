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

class MotifScopeHierarchyProvider : HierarchyProvider {

    override fun browserActivated(hierarchyBrowser: HierarchyBrowser) {
        val browser: MotifScopeHierarchyBrowser = hierarchyBrowser as MotifScopeHierarchyBrowser
        browser.changeView(TypeHierarchyBrowserBase.TYPE_HIERARCHY_TYPE)
    }

    override fun createHierarchyBrowser(target: PsiElement): HierarchyBrowser {
        return MotifScopeHierarchyBrowser(target.project, target as PsiClass)
    }

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