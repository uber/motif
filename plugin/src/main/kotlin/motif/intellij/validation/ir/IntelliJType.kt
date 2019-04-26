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
package motif.intellij.validation.ir

import com.intellij.openapi.project.Project
import com.intellij.psi.GenericsUtil
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiType
import com.intellij.psi.util.InheritanceUtil
import com.intellij.psi.util.TypeConversionUtil
import motif.models.java.IrClass
import motif.models.java.IrType

class IntelliJType(
        private val project: Project,
        private val psiType: PsiType) : IrType {

    override val qualifiedName: String by lazy {
        val canonicalText: String = GenericsUtil.getVariableTypeByExpressionType(psiType).getCanonicalText(true)
        if ('.' in canonicalText) canonicalText else "java.lang.$canonicalText"
    }

    override val isVoid: Boolean by lazy { psiType == PsiType.VOID }

    override fun resolveClass(): IrClass? {
        return (psiType as? PsiClassType)?.let { IntelliJClass(project, it) }
    }

    override fun isAssignableTo(type: IrType): Boolean {
        return TypeConversionUtil.isAssignable((type as IntelliJType).psiType, psiType, false)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IntelliJType

        if (qualifiedName != other.qualifiedName) return false

        return true
    }

    override fun hashCode(): Int {
        return qualifiedName.hashCode()
    }
}