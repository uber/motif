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

import androidx.room.compiler.processing.XExecutableElement
import androidx.room.compiler.processing.XMethodElement
import androidx.room.compiler.processing.XProcessingEnv
import androidx.room.compiler.processing.XType
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.javapoet.KotlinPoetJavaPoetPreview
import com.uber.xprocessing.ext.modifiers
import javax.lang.model.element.Modifier
import motif.compiler.KotlinTypeWorkaround.javaToKotlinType

object XFunSpec {
  /** Copied from [FunSpec.overriding] and modified to leverage [javaToKotlinType]& XProcessing. */
  @OptIn(KotlinPoetJavaPoetPreview::class)
  fun overriding(
      executableElement: XExecutableElement,
      enclosing: XType,
      env: XProcessingEnv,
  ): FunSpec.Builder {
    val methodElement =
        (executableElement as? XMethodElement)
            ?: throw AssertionError("Element is not a method: $executableElement")
    val method = methodElement.asMemberOf(enclosing)

    val returnType =
        if (method.returnType.typeArguments.isNotEmpty()) {
          method.returnType
        } else {
          // ensures that type arguments get loaded
          env.requireType(method.returnType.typeName)
        }

    return overriding(methodElement, method.parameterTypes).returns(javaToKotlinType(returnType))
  }

  private fun overriding(
      method: XMethodElement,
      resolvedParameterTypes: List<XType>,
  ): FunSpec.Builder {
    var modifiers: Set<Modifier> = method.modifiers.toMutableSet()
    require(
        Modifier.PRIVATE !in modifiers &&
            Modifier.FINAL !in modifiers &&
            Modifier.STATIC !in modifiers,
    ) {
      "cannot override method with modifiers: $modifiers"
    }

    val methodName = method.name
    val funBuilder = FunSpec.builder(methodName)

    funBuilder.addModifiers(KModifier.OVERRIDE)

    modifiers = modifiers.toMutableSet()
    modifiers.remove(Modifier.ABSTRACT)
    funBuilder.jvmModifiers(modifiers)

    // TODO: Unsupported until XProcessing is updated
    /*
    method as XParam
        .map { it.asType() as TypeVariable }
        .map { it.asTypeVariableName() }
        .forEach { funBuilder.addTypeVariable(it) }
     */

    method.parameters.forEachIndexed { index, parameter ->
      funBuilder.addParameter(
          ParameterSpec.builder(parameter.name, javaToKotlinType(resolvedParameterTypes[index]))
              .build(),
      )
    }
    if (method.isVarArgs()) {
      funBuilder.parameters[funBuilder.parameters.lastIndex] =
          funBuilder.parameters.last().toBuilder().addModifiers(KModifier.VARARG).build()
    }

    if (method.thrownTypes.isNotEmpty()) {
      val throwsValueString = method.thrownTypes.joinToString { "%T::class" }
      funBuilder.addAnnotation(
          AnnotationSpec.builder(Throws::class)
              .addMember(throwsValueString, *method.thrownTypes.toTypedArray())
              .build(),
      )
    }

    return funBuilder
  }
}
