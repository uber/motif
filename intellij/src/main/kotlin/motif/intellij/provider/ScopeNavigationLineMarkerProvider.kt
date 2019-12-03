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

import com.google.common.collect.Iterables
import com.intellij.codeHighlighting.Pass.UPDATE_ALL
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.markup.GutterIconRenderer.Alignment.LEFT
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.pom.Navigatable
import com.intellij.psi.*
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ConstantFunction
import motif.ast.IrType
import motif.ast.intellij.IntelliJClass
import motif.ast.intellij.IntelliJType
import motif.core.ResolvedGraph
import motif.core.ScopeEdge
import motif.intellij.MotifProjectComponent
import motif.intellij.ScopeHierarchyUtils.Companion.isMotifChildScopeMethod
import motif.intellij.ScopeHierarchyUtils.Companion.isMotifScopeClass
import motif.models.Scope
import java.awt.event.MouseEvent
import com.intellij.ui.awt.RelativePoint
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.ListPopup

/*
 * {@LineMarkerProvider} used to display navigation icons in gutter to navigate to parent/children of Motif scopes.
 */
class ScopeNavigationLineMarkerProvider : LineMarkerProvider, MotifProjectComponent.Listener {

    companion object {
        const val LABEL_NAVIGATE_PARENT_SCOPE: String = "Navigate to parent Scope."
        const val LABEL_NAVIGATE_CHILD_SCOPE: String = "Navigate to child Scope."
        const val MESSAGE_NAVIGATION_NO_SCOPE: String = "Provided class doesn't have a corresponding Motif scope. Please refresh graph manually and try again."
        const val MESSAGE_NAVIGATION_PARENT_ROOT: String = "Can't navigate to parent scope because scope is a root scope."
        const val MESSAGE_TITLE: String = "Motif"
    }

    private var graph: ResolvedGraph? = null

    override fun onGraphUpdated(graph: ResolvedGraph) {
        this.graph = graph
    }

    override fun collectSlowLineMarkers(psiElements: List<PsiElement>,
                                        lineMarkerInfos: Collection<LineMarkerInfo<PsiElement>>) {
    }

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<PsiElement>? {
        if (graph != null) {
            if (element is PsiClass && isMotifScopeClass(element)) {
                val identifier: PsiIdentifier = element.nameIdentifier ?: return null
                return LineMarkerInfo(element, identifier.textRange, AllIcons.Actions.PreviousOccurence, UPDATE_ALL,
                        ConstantFunction<PsiElement, String>(LABEL_NAVIGATE_PARENT_SCOPE),
                        NavigationScopeHandler(element.project, graph!!), LEFT)
            } else if (isMotifChildScopeMethod(element)) {
                return LineMarkerInfo(element, element.textRange, AllIcons.Actions.NextOccurence, UPDATE_ALL,
                        ConstantFunction<PsiElement, String>(LABEL_NAVIGATE_CHILD_SCOPE),
                        NavigationScopeHandler(element.project, graph!!), LEFT)
            }
        }
        return null
    }

    private class NavigationScopeHandler(val project: Project, val graph: ResolvedGraph) : GutterIconNavigationHandler<PsiElement> {
        override fun navigate(event: MouseEvent?, element: PsiElement?) {
            if (element is PsiClass) {
                val scopeType: PsiType = PsiElementFactory.SERVICE.getInstance(project).createType(element)
                val type: IrType = IntelliJType(project, scopeType)
                val scope: Scope? = graph.getScope(type)
                if (scope == null) {
                    Messages.showInfoMessage(MESSAGE_NAVIGATION_NO_SCOPE, MESSAGE_TITLE)
                    return
                }
                val scopeEdges: Array<ScopeEdge> = Iterables.toArray(graph.getParentEdges(scope), ScopeEdge::class.java)
                when (scopeEdges.size) {
                    0 -> Messages.showInfoMessage(MESSAGE_NAVIGATION_PARENT_ROOT, MESSAGE_TITLE)
                    1 -> navigateToParent(scopeEdges[0])
                    else -> {
                        val mouseEvent: MouseEvent = event ?: return
                        val listPopup: ListPopup = JBPopupFactory.getInstance().createListPopup(object : BaseListPopupStep<ScopeEdge>("Select Parent Scope", scopeEdges.toMutableList()) {
                            override fun getTextFor(value: ScopeEdge): String {
                                return value.parent.clazz.simpleName
                            }

                            override fun onChosen(selectedValue: ScopeEdge?, finalChoice: Boolean): PopupStep<*>? {
                                selectedValue?.let { navigateToParent(it) }
                                return super.onChosen(selectedValue, finalChoice)
                            }
                        })
                        listPopup.show(RelativePoint(mouseEvent))
                    }
                }
            } else if (element is PsiMethod) {
                val classElement = PsiTreeUtil.getParentOfType(element, PsiClass::class.java)
                if (isMotifScopeClass(classElement) && element.returnType is PsiClassReferenceType) {
                    val returnElementClass: PsiClass? = (element.returnType as PsiClassReferenceType).resolve()
                    returnElementClass?.let {
                        val navigationElement = it.navigationElement
                        if (navigationElement is Navigatable && (navigationElement as Navigatable).canNavigate()) {
                            navigationElement.navigate(true)
                        }
                    }
                }
            }
        }

        private fun navigateToParent(scopeEdge: ScopeEdge) {
            val navigationElement: PsiElement = (scopeEdge.parent.clazz as IntelliJClass).psiClass.navigationElement
            if (navigationElement is Navigatable && (navigationElement as Navigatable).canNavigate()) {
                navigationElement.navigate(true)
            }
        }
    }
}


