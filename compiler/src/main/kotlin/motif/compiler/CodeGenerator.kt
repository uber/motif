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

import java.io.File
import javax.annotation.processing.ProcessingEnvironment
import motif.core.ResolvedGraph

object CodeGenerator {

  fun generate(env: ProcessingEnvironment, graph: ResolvedGraph, mode: Mode?) {
    val kaptKotlinGeneratedDir = env.options[OPTION_KAPT_KOTLIN_GENERATED]
    if (mode == Mode.JAVA) {
      generateJava(env, graph)
    } else if (mode == Mode.KOTLIN) {
      if (kaptKotlinGeneratedDir == null) {
        throw IllegalStateException(
            "-A$OPTION_MODE=${Mode.KOTLIN.name.toLowerCase()} " +
                "requires -A$OPTION_KAPT_KOTLIN_GENERATED to be set.")
      }
      generateKotlin(env, graph, kaptKotlinGeneratedDir)
    } else {
      if (kaptKotlinGeneratedDir == null) {
        generateJava(env, graph)
      } else {
        generateKotlin(env, graph, kaptKotlinGeneratedDir)
      }
    }
  }

  private fun generateJava(env: ProcessingEnvironment, graph: ResolvedGraph) {
    ScopeImplFactory.create(env, graph)
        .map { scopeImpl -> JavaCodeGenerator.generate(scopeImpl) }
        .forEach { javaFile -> javaFile.writeTo(env.filer) }
  }

  private fun generateKotlin(
      env: ProcessingEnvironment,
      graph: ResolvedGraph,
      kaptKotlinGeneratedDir: String
  ) {
    ScopeImplFactory.create(env, graph)
        .map { scopeImpl -> KotlinCodeGenerator.generate(scopeImpl) }
        .forEach { fileSpec -> fileSpec.writeTo(File(kaptKotlinGeneratedDir)) }
  }
}
