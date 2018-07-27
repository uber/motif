package com.uber.motif.intellij.graph

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiType
import com.uber.motif.intellij.index.ScopeIndex
import com.uber.motif.intellij.psi.getScopeClasses

class GraphProcessor(private val project: Project) {

    private val psiManager = PsiManager.getInstance(project)
    private val index = ScopeIndex.getInstance()


    /**
     * Returns a map of Scope -> List of Parent Scopes
     */
    fun scopeClassesMap(): Map<PsiClass, List<PsiClass>> {

        val returnMap : MutableMap<PsiClass, MutableList<PsiClass>> = mutableMapOf()

        val scopedClasses: List<PsiClass> = scopeClasses()

        // get a mapping of scopeClass -> scopesDeclared

        scopedClasses
                .map {
                    for (classMethod in it.methods) {
                        val psiClass : PsiClass = getClass(classMethod.returnType as PsiType)
                        if (psiClass in scopedClasses) {
                            if (psiClass in returnMap.keys) {
                                returnMap[psiClass]?.add(it)
                            } else {
                                returnMap[psiClass] =  mutableListOf(it)
                            }
                        }
                    }
                }
        return returnMap
    }

    /**
     * Return a list of PsiClasses that are Motif Scopes.
     */
    private fun scopeClasses(): List<PsiClass> {
        return index.getSnapshot(project).files
                .mapNotNull { psiManager.findFile(it) }
                .flatMap { it.getScopeClasses() }
    }

    /**
     * Get a PsiClass from a PsiType
     */
    private fun getClass(psiType: PsiType): PsiClass {
        val psiClassType = psiType as PsiClassType
        return psiClassType.resolve() as PsiClass
    }
}