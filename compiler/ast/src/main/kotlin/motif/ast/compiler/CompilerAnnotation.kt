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
import androidx.room.compiler.processing.XAnnotation
import androidx.room.compiler.processing.XProcessingEnv
import com.uber.xprocessing.ext.isEquivalent
import com.uber.xprocessing.ext.toPrettyString
import kotlin.reflect.KClass
import motif.ast.IrAnnotation
import motif.ast.IrMethod
import motif.ast.IrType

@OptIn(ExperimentalProcessingApi::class)
class CompilerAnnotation(val env: XProcessingEnv, val mirror: XAnnotation) : IrAnnotation {

  override val className: String by lazy {
    mirror.type.typeElement?.qualifiedName
        ?: throw IllegalStateException("Compiler annotation has no qualified class name")
  }

  private val pretty: String by lazy { mirror.toPrettyString() }

  override val type: IrType = CompilerType(env, mirror.type)

  override val members: List<IrMethod> by lazy {
    val annotationMethods = mirror.type.typeElement?.getDeclaredMethods().orEmpty()
    mirror.annotationValues.map { annotationValue ->
      val executableElement =
          annotationMethods.firstOrNull { it.jvmName == annotationValue.name }
              ?: throw IllegalStateException(
                  "No matching annotations for ${annotationValue.name} in ${mirror.annotationValues.map { it.name }.joinToString(separator = ", ")}",
              )
      CompilerMethod(env, mirror.type, executableElement.executableType, executableElement)
    }
  }

  override fun matchesClass(annotationClass: KClass<out Annotation>): Boolean =
      annotationClass.java.name == className

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as CompilerAnnotation

    if (!mirror.isEquivalent(other.mirror, env)) return false

    return true
  }

  override fun hashCode(): Int = pretty.hashCode()

  override fun toString(): String = pretty
}
