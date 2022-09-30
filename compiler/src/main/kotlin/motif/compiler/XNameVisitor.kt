/*
 * Copyright (c) 2022 Uber Technologies, Inc.
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
package motif.compiler

import androidx.room.compiler.processing.XArrayType
import androidx.room.compiler.processing.XProcessingEnv
import androidx.room.compiler.processing.XType
import androidx.room.compiler.processing.compat.XConverters.getProcessingEnv
import androidx.room.compiler.processing.compat.XConverters.toKS
import androidx.room.compiler.processing.isArray
import androidx.room.compiler.processing.isKotlinUnit
import androidx.room.compiler.processing.isVoid
import androidx.room.compiler.processing.ksp.KspTypeMapper
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeVariableName
import com.squareup.javapoet.WildcardTypeName
import com.uber.xprocessing.ext.isDeclaredType
import com.uber.xprocessing.ext.isEnum
import com.uber.xprocessing.ext.isPrimitive

object XNameVisitor {

  fun visit(t: XType): String {
    return when {
      t.isVoid() -> visitNoType(t)
      t.isError() &&
          t.typeElement?.qualifiedName.orEmpty().let { "ERROR" in it || "NonExistent" in it } ->
          visitError(t)
      t.isArray() -> visitArray(t)
      t.isWildcard() -> visitWildcard(t)
      t.isDeclaredType() -> visitDeclared(t)
      t.isPrimitive() -> visitPrimitive(t)
      t.isEnum() -> visitDeclared(t)
      t.isKotlinUnit() -> visitDeclared(t)
      t.isTypeVariable() -> visitTypeVariable(t)
      else -> visitNoType(t)
    }
  }

  private fun visitPrimitive(t: XType): String {
    return when (t.typeName) {
      TypeName.BOOLEAN -> "Boolean"
      TypeName.BYTE -> "Byte"
      TypeName.SHORT -> "Short"
      TypeName.INT -> "Integer"
      TypeName.LONG -> "Long"
      TypeName.CHAR -> "Character"
      TypeName.FLOAT -> "Float"
      TypeName.DOUBLE -> "Double"
      else -> throw IllegalStateException()
    }
  }

  private fun visitDeclared(t: XType, p: Void? = null): String {
    t.typeElement?.kindName()
    val javaQualifiedName =
        KspTypeMapper.getPrimitiveJavaTypeName(t.typeElement?.qualifiedName.orEmpty())
            ?.box()
            ?.toString()
    val simpleName =
        javaQualifiedName?.substringAfterLast(".")
            ?: if ("kotlin.collections.Mutable" in t.typeElement?.qualifiedName.orEmpty()) {
              t.typeElement?.name.orEmpty().replace("Mutable", "")
            } else {
              t.typeElement?.name.orEmpty()
            }
    val enclosingElementString = t.typeElement?.enclosingTypeElement?.name.orEmpty().let {
      return@let if ("kotlin.collections.Mutable" in t.typeElement?.enclosingTypeElement?.qualifiedName.orEmpty()) {
        it.replace("Mutable", "")
      } else {
        it
      }
    }

    val rawString = "$enclosingElementString$simpleName"

    if (t.typeArguments.isEmpty() || t.toString().startsWith("raw ")) {
      return rawString
    }

    if (t.getProcessingEnv().backend == XProcessingEnv.Backend.KSP &&
        t.typeArguments.any { it.isError() }) {
      val raw = t.typeElement?.name.orEmpty()
      val args = t.toKS().arguments.map { "$it".substringAfter(" ") }.joinToString("")
      return "$args$raw"
    }

    val typeArgumentString = t.typeArguments.map { visit(it) }.joinToString("")

    return "$typeArgumentString$rawString"
  }

  private fun visitArray(t: XArrayType, p: Void? = null): String {
    return visit(t.componentType) + "Array"
  }

  private fun visitTypeVariable(t: XType, p: Void? = null): String {
    return t.typeName.toString().capitalize()
  }

  private fun visitWildcard(t: XType, p: Void? = null): String {
    if (t.typeName.toString() == "?" || t.typeName.toString() == "*") {
      return ""
    }
    return t.extendsBound()?.let {
      return visit(it)
    }
        ?: ""
  }

  private fun visitNoType(t: XType): String {
    return if (t.isVoid()) "Void" else defaultAction(t)
  }

  private fun visitError(t: XType, p: Void? = null): String {
    throw IllegalStateException(
        "Could not generate name for ErrorType: $t. Check your code for missing imports or typos.")
  }

  private fun defaultAction(t: XType?): String {
    throw IllegalArgumentException("Unexpected type mirror: $t")
  }
}

private fun XType.isWildcard(): Boolean {
  return typeName is WildcardTypeName
}

private fun XType.isTypeVariable(): Boolean {
  return typeName is TypeVariableName
}
