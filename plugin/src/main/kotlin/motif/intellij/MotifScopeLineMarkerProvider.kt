package motif.intellij

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.psi.*
import motif.intellij.icons.Icons
import kotlin.system.measureNanoTime


class MotifScopeLineMarkerProvider : RelatedItemLineMarkerProvider() {

    val visited: MutableSet<PsiClass> = mutableSetOf()

    fun visitAnnotation(annotation: PsiAnnotation) {
        annotation.text
    }

    fun visitType(type: PsiType) {
        type.canonicalText
        type.annotations.forEach(::visitAnnotation)
    }

    fun visitSignature(signature: HierarchicalMethodSignature) {
        signature.parameterTypes.forEach(::visitType)
        signature.method.returnType?.let(::visitType)
    }

    fun visitClass(psiClass: PsiClass) {
        psiClass.visibleSignatures.forEach(::visitSignature)
    }

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<PsiElement>>) {

        if (element !is PsiMethod && element !is PsiIdentifier) return

        val component = MotifComponent.get(element.project)
        val graphProcessor = component.graphProcessor
        val scopeClassesMap = graphProcessor.scopeClassesMap()
        scopeClassesMap.forEach { scopeClass, _ ->
            val alreadyVisited = !visited.add(scopeClass)
            val dur = measureNanoTime {
                scopeClass.let(::visitClass)
                scopeClass.innerClasses.find { it.name == "Objects" }!!.let(::visitClass)
            }
            println("$alreadyVisited: $dur: $scopeClass")
        }

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
