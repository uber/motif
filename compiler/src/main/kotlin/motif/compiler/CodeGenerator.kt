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
import androidx.room.compiler.processing.writeTo
import java.io.File
import motif.core.ResolvedGraph

object CodeGenerator {

  fun generate(env: XProcessingEnv, graph: ResolvedGraph, mode: OutputMode?): List<String> {
    val kaptKotlinGeneratedDir = env.options[OPTION_KAPT_KOTLIN_GENERATED]
    return if (mode == OutputMode.JAVA) {
      generateJava(env, graph)
    } else if (mode == OutputMode.KOTLIN) {
      if (env.backend == XProcessingEnv.Backend.JAVAC && kaptKotlinGeneratedDir == null) {
        throw IllegalStateException(
            "-A$OPTION_MODE=${OutputMode.KOTLIN.name.lowercase()} " +
                "requires -A$OPTION_KAPT_KOTLIN_GENERATED to be set.",
        )
      }
      generateKotlin(env, graph, kaptKotlinGeneratedDir)
    } else {
      if (env.backend == XProcessingEnv.Backend.KSP) {
        generateKotlin(env, graph, kaptKotlinGeneratedDir)
      } else {
        if (kaptKotlinGeneratedDir == null) {
          generateJava(env, graph)
        } else {
          generateKotlin(env, graph, kaptKotlinGeneratedDir)
        }
      }
    }
  }

  private fun generateJava(env: XProcessingEnv, graph: ResolvedGraph): List<String> =
      ScopeImplFactory.create(env, graph)
          .map { scopeImpl -> JavaCodeGenerator.generate(scopeImpl) }
          .onEach { javaFile -> javaFile.writeTo(env.filer) }
          .map { "${it.packageName}.${it.typeSpec.name}" }

  private fun generateKotlin(
      env: XProcessingEnv,
      graph: ResolvedGraph,
      kaptKotlinGeneratedDir: String? = null,
  ): List<String> =
      ScopeImplFactory.create(env, graph)
          .map { scopeImpl -> KotlinCodeGenerator.generate(scopeImpl) }
          .onEach { fileSpec ->
            if (kaptKotlinGeneratedDir != null) {
              fileSpec.writeTo(File(kaptKotlinGeneratedDir))
            } else {
              fileSpec.writeTo(env.filer)
            }
          }
          .map { "${it.packageName}.${it.name}" }
}

enum class OutputMode {
  JAVA, // Generate pure Java implementation
  KOTLIN, // Generate pure Kotlin implementation
}
