package com.uber.motif.intellij.psi

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaFile

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
    return annotations.mapNotNull { it?.nameReferenceElement?.referenceName }
            .find { it == "Scope" || it == "com.uber.motif.Scope" } != null
}