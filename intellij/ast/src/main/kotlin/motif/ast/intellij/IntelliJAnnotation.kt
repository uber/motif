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
package motif.ast.intellij

import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import motif.ast.IrAnnotation
import motif.ast.IrType
import kotlin.jvm.java
import kotlin.jvm.javaClass
import kotlin.reflect.KClass

class IntelliJAnnotation(
        private val project: Project,
        private val psiAnnotation: PsiAnnotation) : IrAnnotation {

    override val type: IrType by lazy {
        val qualifiedName: String = psiAnnotation.nameReferenceElement!!.qualifiedName
        val annotationClass: PsiClass = JavaPsiFacade.getInstance(project).findClass(qualifiedName, GlobalSearchScope.allScope(project))!!
        val psiClassType: PsiClassType = PsiElementFactory.SERVICE.getInstance(project).createType(annotationClass)
        IntelliJType(project, psiClassType)
    }

    override fun matchesClass(annotationClass: KClass<out Annotation>): Boolean {
        return psiAnnotation.qualifiedName == annotationClass.java.name
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IntelliJAnnotation

        if (psiAnnotation.text != other.psiAnnotation.text) return false

        return true
    }

    override fun hashCode(): Int {
        return psiAnnotation.text.hashCode()
    }
}