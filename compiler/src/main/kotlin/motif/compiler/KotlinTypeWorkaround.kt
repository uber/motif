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
package motif.compiler

import androidx.room.compiler.processing.XProcessingEnv
import androidx.room.compiler.processing.XType
import androidx.room.compiler.processing.compat.XConverters.getProcessingEnv
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.javapoet.KotlinPoetJavaPoetPreview
import com.squareup.kotlinpoet.javapoet.toKTypeName
import kotlin.reflect.jvm.internal.impl.builtins.jvm.JavaToKotlinClassMap
import kotlin.reflect.jvm.internal.impl.name.FqName

/** Workarounds for: https://github.com/square/kotlinpoet/issues/236 */
@OptIn(KotlinPoetJavaPoetPreview::class)
object KotlinTypeWorkaround {

  fun javaToKotlinType(mirror: XType): TypeName {
    return javaToKotlinType(asTypeName(mirror), mirror.getProcessingEnv())
  }

  fun javaToKotlinType(
      className: com.squareup.kotlinpoet.ClassName,
      env: XProcessingEnv?
  ): TypeName {
    return javaToKotlinType(className as TypeName, env)
  }

  private fun asTypeName(mirror: XType): TypeName {
    return mirror.typeName.toKTypeName()
  }

  /** https://github.com/square/kotlinpoet/issues/236#issuecomment-437961476 */
  private fun javaToKotlinType(typeName: TypeName, env: XProcessingEnv?): TypeName {
    return when (typeName) {
      is ParameterizedTypeName ->
          (javaToKotlinType(typeName.rawType, null) as com.squareup.kotlinpoet.ClassName)
              .parameterizedBy(
                  *typeName.typeArguments.map { javaToKotlinType(it, env) }.toTypedArray())
      is WildcardTypeName ->
          when {
            typeName.inTypes.isNotEmpty() ->
                WildcardTypeName.consumerOf(javaToKotlinType(typeName.inTypes.single(), env))
            typeName.outTypes.isNotEmpty() ->
                WildcardTypeName.producerOf(javaToKotlinType(typeName.outTypes.single(), env))
            else -> throw IllegalStateException()
          }
      is TypeVariableName -> STAR
      else -> {
        val className =
            JavaToKotlinClassMap.INSTANCE
                .mapJavaToKotlin(FqName(typeName.toString()))
                ?.asSingleFqName()
                ?.asString()
        if (className == null) {
          typeName.withRawTypeFix(env)
        } else {
          com.squareup.kotlinpoet.ClassName.bestGuess(className)
        }
      }
    }
  }

  private fun TypeName.withRawTypeFix(env: XProcessingEnv?): TypeName {
    if (env?.backend == XProcessingEnv.Backend.KSP && this is com.squareup.kotlinpoet.ClassName) {
      val mirror = env.findType(this.toString())
      if (mirror != null && mirror.typeArguments.isNotEmpty() && "<" !in this.toString()) {
        return this.parameterizedBy(mirror.typeArguments.map { STAR })
      }
    }
    return this
  }
}
