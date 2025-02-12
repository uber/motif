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
package motif.compiler.ksp

import androidx.room.compiler.processing.ExperimentalProcessingApi
import androidx.room.compiler.processing.XProcessingEnvConfig
import androidx.room.compiler.processing.ksp.KspBasicAnnotationProcessor
import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import motif.compiler.MotifProcessingStep
import motif.core.ResolvedGraph

@AutoService(SymbolProcessorProvider::class)
class MotifSymbolProcessorProvider(
    private val config: XProcessingEnvConfig = XProcessingEnvConfig.DEFAULT,
) : SymbolProcessorProvider {
  lateinit var graph: ResolvedGraph

  override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
      MotifSymbolProcessor(environment, config)

  @OptIn(ExperimentalProcessingApi::class)
  private inner class MotifSymbolProcessor(
      environment: SymbolProcessorEnvironment,
      config: XProcessingEnvConfig,
  ) : KspBasicAnnotationProcessor(environment, config) {

    override fun processingSteps() = listOf(MotifProcessingStep(graphSetter = { graph = it }))
  }
}
