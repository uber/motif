package com.uber.motif.intellij.psi

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaFile
import com.uber.motif.Scope

/**
 * Returns list of all PsiClass objects that represent a Motif Scope.
 */
fun PsiFile.getScopeClasses(): List<PsiClass> {
    val psiJavaFile: PsiJavaFile = this as? PsiJavaFile ?: return listOf()
    return psiJavaFile.classes.filter { it.isScopeClass() }
}

/**
 * Returns true if and only if this class is a Motif Scope. Unlike PsiClass.isMaybeScopeClass(), this is not safe to
 * call while indexing and does not return false positives.
 */
fun PsiClass.isScopeClass(): Boolean {
    return annotations.find { it.qualifiedName == Scope::class.qualifiedName} != null
}

/**
 * Returns true for all Motif Scope files, but also returns a false positive if the @Scope annotation is not a
 * Motif Scope. This is to avoid having to resolve the annotations' qualified name so we can call this while indexing.
 */
fun PsiFile.isMaybeScopeFile(): Boolean {
    val psiJavaFile: PsiJavaFile = this as? PsiJavaFile ?: return false
    return psiJavaFile.classes.find { it.isMaybeScopeClass() } != null
}

/**
 * Returns true for all Motif Scope classes, but also returns a false positive if the @Scope annotation is not a
 * Motif Scope. This is to avoid having to resolve the annotations' qualified name so we can call this while indexing.
 */
fun PsiClass.isMaybeScopeClass(): Boolean {
    return annotations.mapNotNull { it.nameReferenceElement?.referenceName }
            .find { it == "Scope" || it == "com.uber.motif.Scope" } != null
}