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
package motif.intellij.provider

import com.intellij.codeHighlighting.Pass.UPDATE_ALL
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.markup.GutterIconRenderer.Alignment.LEFT
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.PsiMethod
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.util.ConstantFunction
import motif.intellij.MotifProjectComponent
import motif.intellij.MotifProjectComponent.Companion.TOOL_WINDOW_ID
import motif.intellij.ScopeHierarchyUtils.Companion.isMotifScopeClass
import java.awt.event.MouseEvent

/*
 * {@LineMarkerProvider} used to display icon in gutter to navigate to motif scope ancestors hierarchy.
 */
class ScopeHierarchyLineMarkerProvider : LineMarkerProvider {

    companion object {
        const val LABEL_ANCESTORS_SCOPE: String = "View Scope Ancestors."
    }

    override fun collectSlowLineMarkers(psiElements: List<PsiElement>,
                                        lineMarkerInfos: Collection<LineMarkerInfo<PsiElement>>) {
    }

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<PsiElement>? {
        if (element !is PsiClass) {
            return null
        }
        if (!isMotifScopeClass(element)) {
            return null
        }
        val identifier: PsiIdentifier = element.nameIdentifier ?: return null
        return LineMarkerInfo(element, identifier.textRange, AllIcons.Hierarchy.Supertypes, UPDATE_ALL,
                ConstantFunction<PsiElement, String>(LABEL_ANCESTORS_SCOPE), ScopeHierarchyHandler(element.project), LEFT)
    }

    private class ScopeHierarchyHandler(val project: Project) : GutterIconNavigationHandler<PsiElement> {
        override fun navigate(event: MouseEvent?, element: PsiElement?) {
            val toolWindow: ToolWindow = ToolWindowManager.getInstance(project).getToolWindow(TOOL_WINDOW_ID)
            if (element is PsiClass) {
                toolWindow.activate {
                    MotifProjectComponent.getInstance(project).onSelectedAncestorScope(element)
                }
            } else if (element is PsiMethod) {
                if (element.returnType is PsiClassReferenceType) {
                    val returnElementClass: PsiClass = (element.returnType as PsiClassReferenceType).resolve() ?: return
                    toolWindow.activate {
                        MotifProjectComponent.getInstance(project).onSelectedAncestorScope(returnElementClass)
                    }
                }
            }
        }
    }
}


