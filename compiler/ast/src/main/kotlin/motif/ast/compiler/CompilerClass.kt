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
package motif.ast.compiler

import androidx.room.compiler.processing.ExperimentalProcessingApi
import androidx.room.compiler.processing.XExecutableType
import androidx.room.compiler.processing.XProcessingEnv
import androidx.room.compiler.processing.XType
import androidx.room.compiler.processing.XTypeElement
import com.uber.xprocessing.ext.getLocalAndInheritedMethods
import com.uber.xprocessing.ext.isDeclaredType
import com.uber.xprocessing.ext.isInternal
import com.uber.xprocessing.ext.typeUtils
import motif.ast.IrAnnotation
import motif.ast.IrClass
import motif.ast.IrField
import motif.ast.IrMethod
import motif.ast.IrModifier
import motif.ast.IrType
import java.util.Collections

@OptIn(ExperimentalProcessingApi::class)
class CompilerClass(
  override val env: XProcessingEnv,
  val declaredType: XType
) : IrUtil, IrClass {

  private val typeElement: XTypeElement by lazy { declaredType.typeElement as XTypeElement }

  override val type: IrType by lazy { CompilerType(env, declaredType) }

  override val supertypes: List<IrType> by lazy {
    env.typeUtils.directSupertypes(declaredType).map { CompilerType(env, it) }
  }

  override val typeArguments: List<IrType> by lazy {
    declaredType.typeArguments.map { CompilerType(env, it) }
  }

  override val kind: IrClass.Kind by lazy {
    when {
      typeElement.isClass() -> IrClass.Kind.CLASS
      typeElement.isInterface() -> IrClass.Kind.INTERFACE
      else -> throw IllegalStateException()
    }
  }

  override val methods: List<IrMethod> by lazy {
    val methods = typeElement.getLocalAndInheritedMethods(env, useMoreElements = false)
    val nonPrivateStaticMethods =
        typeElement.getDeclaredMethods().filter { it.isStatic() && !it.isPrivate() }
    (methods + nonPrivateStaticMethods)
        .map { executableElement ->
          val executableType = executableElement.asMemberOf(declaredType) as XExecutableType
          CompilerMethod(env, declaredType, executableType, executableElement)
        }
        .distinctBy {
          "${it.annotations.joinToString(separator = ",")} ${it.name}${it.parameters.map { it.type.toString() }.joinToString(separator = ",", prefix = "(", postfix = ")")}"
        }
        .toList()
  }

  override val constructors: List<IrMethod> by lazy {
    val constructors =
        try {
          typeElement.getConstructors()
        } catch (e: UnsupportedOperationException) {
          if (env.backend == XProcessingEnv.Backend.KSP) {
            listOfNotNull(typeElement.findPrimaryConstructor())
          } else {
            throw e
          }
        }
    constructors.map { executableElement ->
      val executableType = executableElement.asMemberOf(declaredType)
      CompilerMethod(env, declaredType, executableType, executableElement)
    }
  }

  override val nestedClasses: List<IrClass> by lazy {
    typeElement.getEnclosedTypeElements().map { typeElement ->
      if (typeElement.type.isError()) {
        throw IllegalStateException(
            "Could not resolve type for nested class: ${typeElement.qualifiedName}",
        )
      }
      CompilerClass(
        env,
        typeElement.type
      )
    }
  }

  override val fields: List<IrField> by lazy { declaredType.getAllFields() }

  override val annotations: List<IrAnnotation> by lazy { typeElement.irAnnotations() }

  override val modifiers: Set<IrModifier> by lazy { typeElement.irModifiers() }

  private fun XType.getAllFields(): List<IrField> {
    val typeElement: XTypeElement =
        this.typeElement ?: throw IllegalStateException("No type element for type: $this")
    val fields: MutableList<IrField> =
        typeElement
            .getDeclaredFields()
            .map { variableElement -> CompilerField(env, variableElement) }
            .toMutableList()

    val superclass = typeElement.superType
    if (superclass?.isDeclaredType() == true) {
      fields += superclass.getAllFields()
    } else if (superclass != null && !superclass.isNone()) {
      // TODO Is it possible for TypeElement.superclass to return a TypeMirror of TypeKind other
      // than
      // DECLARED or NONE? If so, then we should not throw an Exception here and instead handle the
      // recursion properly.
      throw IllegalStateException("Unknown superclass type.")
    }

    return fields
  }

  fun isInternal() = declaredType.isInternal()

  companion object {
    internal val TOP_LEVEL_OBJECT_NAMES = setOf("java.lang.Object", "kotlin.Any", "Any", "Object")
  }
}
