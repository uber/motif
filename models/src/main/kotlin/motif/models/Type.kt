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
package motif.models

import javax.inject.Qualifier
import motif.ast.IrAnnotated
import motif.ast.IrAnnotation
import motif.ast.IrClass
import motif.ast.IrMethod
import motif.ast.IrParameter
import motif.ast.IrType

/**
 * This is the primitive type representation that Motif operates on. It's a data class so .equals()
 * is valid for checking matching Types.
 */
data class Type(val type: IrType, val qualifier: IrAnnotation?) : Comparable<Type> {

  val qualifiedName: String by lazy {
    val qualifierString = qualifier?.let { "$it " } ?: ""
    "$qualifierString${type.qualifiedName}"
  }

  val simpleName: String by lazy {
    val qualifierString = qualifier?.let { "$it " } ?: ""
    "$qualifierString${type.simpleName}"
  }

  override fun compareTo(other: Type): Int = compareKey.compareTo(other.compareKey)

  private val compareKey: String by lazy {
    val qualifierString = qualifier?.let { "$it " } ?: ""
    "${type.qualifiedName}$qualifierString"
  }

  companion object {

    fun fromParameter(parameter: IrParameter): Type = Type(parameter.type, parameter.getQualifier())

    fun fromReturnType(method: IrMethod): Type = Type(method.returnType, method.getQualifier())

    private fun IrAnnotated.getQualifier(): IrAnnotation? {
      val qualifier =
          annotations.find { annotation ->
            val annotationClass: IrClass = annotation.type?.resolveClass() ?: return@find false
            annotationClass.hasAnnotation(Qualifier::class)
          } ?: return null

      val members = qualifier.members
      if (members.size > 1) {
        throw InvalidQualifier(this, qualifier)
      }

      if (members.size == 1) {
        val member = members[0]
        if (member.name !in setOf("value", "getValue") ||
            member.returnType.qualifiedName !in STRING_NAMES) {
          throw InvalidQualifier(this, qualifier)
        }
      }

      return qualifier
    }

    private val STRING_NAMES = setOf("java.lang.String", "kotlin.String", "String")
  }
}
