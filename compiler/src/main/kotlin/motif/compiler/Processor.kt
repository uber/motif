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

import androidx.room.compiler.processing.XProcessingStep
import androidx.room.compiler.processing.javac.JavacBasicAnnotationProcessor
import javax.lang.model.SourceVersion
import motif.core.ResolvedGraph

const val OPTION_KAPT_KOTLIN_GENERATED = "kapt.kotlin.generated"
const val OPTION_MODE = "motif.mode"

class Processor : JavacBasicAnnotationProcessor() {
  lateinit var graph: ResolvedGraph

  override fun getSupportedSourceVersion(): SourceVersion {
    return SourceVersion.latestSupported()
  }

  override fun processingSteps(): Iterable<XProcessingStep> {
    return listOf<XProcessingStep>(MotifProcessingStep(graphSetter = { graph = it }))
  }

  override fun getSupportedOptions(): Set<String> {
    return setOf(OPTION_MODE, OPTION_KAPT_KOTLIN_GENERATED)
  }
}
