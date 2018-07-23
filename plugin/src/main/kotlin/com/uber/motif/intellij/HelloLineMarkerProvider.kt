package com.uber.motif.intellij

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier

class HelloLineMarkerProvider : LineMarkerProvider {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element !is PsiIdentifier) return null

        val parent: PsiClass = element.parent as? PsiClass ?: return null
        val component = MotifComponent.get(element.project)

        val scopeClasses = component.graphProcessor.scopeClasses()
        return if (parent in scopeClasses) {
            NavigationGutterIconBuilder.create(AllIcons.Mac.Tree_white_down_arrow)
                    .setTarget(element)
                    .createLineMarkerInfo(element)
        } else {
            null
        }
    }

    override fun collectSlowLineMarkers(elements: MutableList<PsiElement>, result: MutableCollection<LineMarkerInfo<PsiElement>>) {}
}