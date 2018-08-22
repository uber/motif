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
package motif.intellij.psi

import com.intellij.psi.*
import motif.Scope

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
            .find { it == "Scope" || it == "motif.Scope" } != null
}

/**
 * Get a PsiClass from a PsiType
 */
fun PsiType.getClass(): PsiClass? {
    if (this is PsiClassType) return this.resolve()
    return null
}
