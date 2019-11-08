/*
 * Copyright (c) 2018-2019 Uber Technologies, Inc.
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
package motif.ast.intellij

import com.intellij.codeInsight.AnnotationUtil
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import motif.ast.IrAnnotation
import motif.ast.IrMethod
import motif.ast.IrType
import kotlin.jvm.java
import kotlin.jvm.javaClass
import kotlin.reflect.KClass

class IntelliJAnnotation(
        private val project: Project,
        private val psiAnnotation: PsiAnnotation) : IrAnnotation {

    private val stringValue: String? by lazy {
        getStringConstantValue(project, psiAnnotation, "value")
    }

    override val className: String by lazy {
        psiAnnotation.qualifiedName!!
    }

    private val key: Key by lazy {
        Key(className, stringValue)
    }

    override val type: IrType? by lazy {
        val nameReferenceElement = psiAnnotation.nameReferenceElement
        val annotationClass: PsiClass = nameReferenceElement?.resolve() as? PsiClass ?: return@lazy null
        val psiClassType: PsiClassType = PsiElementFactory.SERVICE.getInstance(project).createType(annotationClass)
        IntelliJType(project, psiClassType)
    }

    override val members: List<IrMethod> by lazy {
        psiAnnotation.parameterList.attributes
                .map {
                    it.reference!!.resolve() as PsiMethod
                }
                .map { IntelliJMethod(project, it, PsiSubstitutor.EMPTY) }
    }

    override fun matchesClass(annotationClass: KClass<out Annotation>): Boolean {
        return psiAnnotation.qualifiedName == annotationClass.java.name
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as IntelliJAnnotation
        return key == other.key
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }

    override fun toString(): String {
        val value = stringValue?.let { "(\"$it\")" } ?: ""
        return "@${psiAnnotation.qualifiedName!!}$value"
    }

    companion object {

        private fun getStringConstantValue(
                project: Project,
                annotation: PsiAnnotation,
                attributeName: String): String? {
            val value = annotation.findAttributeValue(attributeName) ?: return null

            AnnotationUtil.getStringAttributeValue(value)?.let { return it }

            if (value !is PsiReferenceExpression) return null

            val referenceTarget = value.resolve() ?: return null

            if (referenceTarget !is PsiField) return null

            val constant = JavaPsiFacade.getInstance(project).constantEvaluationHelper
                    .computeConstantExpression(referenceTarget.initializer) ?: return null

            return constant as? String
        }
    }

    private data class Key(val className: String, val value: String?)
}
