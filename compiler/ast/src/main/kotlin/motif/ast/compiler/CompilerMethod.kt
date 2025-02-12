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
import androidx.room.compiler.processing.XConstructorElement
import androidx.room.compiler.processing.XConstructorType
import androidx.room.compiler.processing.XExecutableElement
import androidx.room.compiler.processing.XExecutableType
import androidx.room.compiler.processing.XMethodElement
import androidx.room.compiler.processing.XMethodType
import androidx.room.compiler.processing.XProcessingEnv
import androidx.room.compiler.processing.XType
import androidx.room.compiler.processing.compat.XConverters.toKS
import androidx.room.compiler.processing.isConstructor
import androidx.room.compiler.processing.isVoid
import com.uber.xprocessing.ext.makeNonNullByDefault
import motif.ast.IrAnnotation
import motif.ast.IrMethod
import motif.ast.IrModifier
import motif.ast.IrType

@OptIn(ExperimentalProcessingApi::class)
class CompilerMethod(
    override val env: XProcessingEnv,
    val owner: XType,
    val type: XExecutableType,
    val element: XExecutableElement,
) : IrMethod, IrUtil {

  override val name: String =
      when (element) {
        is XMethodElement -> element.jvmName
        is XConstructorElement -> "<init>"
        else ->
            throw IllegalStateException(
                "Could not find name for unknown XExecutableElement kind: ${element.kindName()}",
            )
      }

  override val isConstructor: Boolean = element.isConstructor()

  override val annotations: List<IrAnnotation> by lazy { element.irAnnotations() }

  override val modifiers: Set<IrModifier> by lazy { element.irModifiers() }

  override val parameters: List<CompilerMethodParameter> by lazy {
    val parameters = element.parameters
    val types = type.parameterTypes
    (0 until parameters.size).map { i -> CompilerMethodParameter(env, parameters[i], types[i]) }
  }

  override val returnType: IrType by lazy {
    val returnType =
        when (type) {
          is XMethodType -> {
            val returnXType = type.returnType.makeNonNullByDefault()
            CompilerType(env, returnXType)
          }
          is XConstructorType -> CompilerType(env, owner)
          else -> throw IllegalStateException("Compiler method has no return type")
        }
    returnType
  }

  override fun isVoid(): Boolean =
      when (env.backend) {
        XProcessingEnv.Backend.JAVAC -> returnType.isVoid
        XProcessingEnv.Backend.KSP -> {
          val returnTypeMirror = (returnType as CompilerType).mirror
          if (returnTypeMirror.isError()) {
            element.toKS().returnType == null
          } else {
            returnTypeMirror.isVoid()
          }
        }
      }

  val isSynthetic by lazy {
    env.backend == XProcessingEnv.Backend.KSP && "synthetic" in element.executableType.toString()
  }
}
