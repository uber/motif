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

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.psi.*
import motif.intellij.MotifComponent
import motif.intellij.getClass
import motif.intellij.isScopeClass

/**
 * This class is invoked when rendering the gutter on the side of a class in IntelliJ. It determines the pieces of
 * code that are Motif scopes and adds markers to them. For a Motif Scope class, it adds a marker to navigate to the
 * parents where the current scope is declared, and next to the child scopes to navigate to their definition.
 */
class MotifScopeLineMarkerProvider : RelatedItemLineMarkerProvider() {

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<PsiElement>>) {

        if (element !is PsiMethod && element !is PsiIdentifier) return

        val component = MotifComponent.get(element.project)
        val graphProcessor = component.graphProcessor
        val scopeClassesMap = graphProcessor.scopeToParentsMap()

        when(element) {
            // the case when the class itself is a Motif Scope
            is PsiIdentifier -> {
                val scopeClass = element.parent as? PsiClass ?: return
                scopeClassesMap.entries
                        .find { it.key == scopeClass }
                        ?.let { (_, parentScopeClasses) ->
                            val parentTargets: MutableList<PsiMethod> = mutableListOf()
                            parentScopeClasses
                                    .forEach { parentClass ->
                                        parentClass.methods
                                                .first { it.returnType!!.getClass() == scopeClass }
                                                .let { parentTargets.add(it) }
                                    }
                            val builder: NavigationGutterIconBuilder<PsiElement> =
                                    NavigationGutterIconBuilder
                                            .create(Icons.PARENT_SCOPES)
                                            .setTooltipTitle("Navigate to Parent")
                                            .setTargets(parentTargets)
                            result.add(builder.createLineMarkerInfo(element))
                        }
            }
            // the case when a child scope is declared in a class
            is PsiMethod -> {
                val returnType = element.returnType ?: return
                if (element.containingClass?.isScopeClass() == true) {
                    scopeClassesMap.entries
                            .find { it.key == returnType.getClass() }
                            ?.let {
                                val builder: NavigationGutterIconBuilder<PsiElement> =
                                        NavigationGutterIconBuilder
                                                .create(Icons.CHILD_SCOPE)
                                                .setTooltipTitle("Navigate to Definition")
                                                .setTargets(it.key)
                                result.add(builder.createLineMarkerInfo(element))
                            }
                }
            }
        }
    }
}
