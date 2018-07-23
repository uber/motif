package com.uber.motif.intellij

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement

class HelloLineMarkerProvider : LineMarkerProvider {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val component = MotifComponent.get(element.project)
        return if (element in component.graphProcessor.scopeClasses()) {
            NavigationGutterIconBuilder.create(AllIcons.Mac.Tree_white_down_arrow)
                    .setTarget(element)
                    .createLineMarkerInfo(element)
        } else {
            null
        }
    }

    override fun collectSlowLineMarkers(elements: MutableList<PsiElement>, result: MutableCollection<LineMarkerInfo<PsiElement>>) {}
}