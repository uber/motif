package com.uber.motif.intellij

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
import com.uber.motif.intellij.icons.Icons



class HelloLineMarkerProvider : RelatedItemLineMarkerProvider() {

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<PsiElement>>) {
        if (element !is PsiIdentifier) return

        val scopeClass = element.parent as? PsiClass ?: return
        val component = MotifComponent.get(element.project)
        val scopeClassesMap = component.graphProcessor.scopeClassesMap()

        scopeClassesMap.entries
                .find { it.key == scopeClass }
                ?.let { (_, parentScopeClasses) ->
                    val builder: NavigationGutterIconBuilder<PsiElement> =
                            NavigationGutterIconBuilder
                                    .create(Icons.PARENT_SCOPES)
                                    .setTargets(parentScopeClasses)
                    result.add(builder.createLineMarkerInfo(element))
                }
    }
}