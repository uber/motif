package motif.intellij

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.PsiMethod
import motif.intellij.icons.Icons



class MotifScopeLineMarkerProvider : RelatedItemLineMarkerProvider() {

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<PsiElement>>) {

        if (element !is PsiMethod && element !is PsiIdentifier) return

        val component = MotifComponent.get(element.project)
        val graphProcessor = component.graphProcessor
        val scopeClassesMap = graphProcessor.scopeClassesMap()

        when(element) {
            is PsiIdentifier -> {
                val scopeClass = element.parent as? PsiClass ?: return
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
            is PsiMethod -> {
                val returnType = element.returnType ?: return
                scopeClassesMap.entries
                        .find { it.key == graphProcessor.getClass(returnType) }
                        ?.let {
                            val builder: NavigationGutterIconBuilder<PsiElement> =
                                    NavigationGutterIconBuilder
                                            .create(Icons.CHILD_SCOPE)
                                            .setTargets(it.key)
                            result.add(builder.createLineMarkerInfo(element))
                        }
            }
        }
    }
}
