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

import motif.compiler.processing.ExperimentalProcessingApi
import motif.compiler.processing.XElement
import motif.compiler.processing.XMessager
import motif.compiler.processing.XProcessingEnv
import motif.compiler.processing.XProcessingStep
import motif.core.ResolvedGraph

@ExperimentalProcessingApi
class MotifProcessingStep(
    private val graphSetter: (ResolvedGraph) -> Unit,
    private val messageWatcher: XMessager? = null,
) : XProcessingStep {

  override fun annotations() = mutableSetOf(motif.Scope::class.qualifiedName!!)

  override fun process(
      env: XProcessingEnv,
      elementsByAnnotation: Map<String, Set<XElement>>
  ): Set<XElement> {
    messageWatcher?.let { env.messager.addMessageWatcher(messageWatcher) }

    // TODO: implement processor in next PR
    graphSetter.invoke(ResolvedGraph.create(emptyList()))

    return emptySet()
  }

  override fun processOver(env: XProcessingEnv, elementsByAnnotation: Map<String, Set<XElement>>) {
    process(env, elementsByAnnotation)
  }
}
