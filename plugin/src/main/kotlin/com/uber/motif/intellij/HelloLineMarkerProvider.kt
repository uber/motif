package com.uber.motif.intellij

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
import com.uber.motif.intellij.icons.Icons

class HelloLineMarkerProvider : LineMarkerProvider {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element !is PsiIdentifier) return null

        val parent: PsiClass = element.parent as? PsiClass ?: return null
        val component = MotifComponent.get(element.project)

        val scopeClasses = component.graphProcessor.scopeClasses()
        return if (parent in scopeClasses) {
            NavigationGutterIconBuilder.create(Icons.CHILD_SCOPE)
                    .setTarget(element)
                    .createLineMarkerInfo(element)
        } else {
            null
        }
    }

    override fun collectSlowLineMarkers(elements: MutableList<PsiElement>, result: MutableCollection<LineMarkerInfo<PsiElement>>) {}
}