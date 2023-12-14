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

import com.intellij.openapi.project.Project
import com.intellij.psi.GenericsUtil
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiType
import com.intellij.psi.PsiTypes
import com.intellij.psi.util.TypeConversionUtil
import kotlin.jvm.javaClass
import kotlin.let
import motif.ast.IrClass
import motif.ast.IrType

class IntelliJType(private val project: Project, val psiType: PsiType) : IrType {

  override val qualifiedName: String by lazy {
    GenericsUtil.getVariableTypeByExpressionType(psiType).getCanonicalText(false)
  }

  override val isVoid: Boolean by lazy { psiType == PsiTypes.voidType() }

  override val isPrimitive: Boolean by lazy {
    when (psiType) {
      PsiTypes.booleanType(),
      PsiTypes.byteType(),
      PsiTypes.shortType(),
      PsiTypes.intType(),
      PsiTypes.longType(),
      PsiTypes.charType(),
      PsiTypes.floatType(),
      PsiTypes.doubleType() -> true
      else -> false
    }
  }

  override fun resolveClass(): IrClass? {
    val psiClassType = psiType as? PsiClassType ?: return null
    val psiClass = psiClassType.resolve() ?: return null
    return (psiType as? PsiClassType)?.let { IntelliJClass(project, it, psiClass) }
  }

  override fun isAssignableTo(type: IrType): Boolean {
    return TypeConversionUtil.isAssignable((type as IntelliJType).psiType, psiType, false)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as IntelliJType

    if (psiType != other.psiType) return false

    return true
  }

  override fun hashCode(): Int {
    return psiType.hashCode()
  }
}
