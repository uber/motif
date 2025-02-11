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

import androidx.room.compiler.processing.ExperimentalProcessingApi
import androidx.room.compiler.processing.XElement
import androidx.room.compiler.processing.XMessager
import androidx.room.compiler.processing.XProcessingEnv
import androidx.room.compiler.processing.XProcessingStep
import androidx.room.compiler.processing.XTypeElement
import javax.tools.Diagnostic
import motif.Scope
import motif.ast.compiler.CompilerClass
import motif.ast.compiler.CompilerType
import motif.core.AlreadySatisfiedError
import motif.core.DependencyCycleError
import motif.core.ResolvedGraph
import motif.errormessage.ErrorMessage

@ExperimentalProcessingApi
class MotifProcessingStep(
    private val graphSetter: (ResolvedGraph) -> Unit,
    private val messageWatcher: XMessager? = null,
) : XProcessingStep {
  private val initialScopeNames = mutableSetOf<String>()
  private val createdScopeNames = mutableSetOf<String>()

  override fun annotations() = mutableSetOf(motif.Scope::class.qualifiedName!!)

  override fun process(
      env: XProcessingEnv,
      elementsByAnnotation: Map<String, Set<XElement>>,
      isLastRound: Boolean
  ): Set<XElement> {
    messageWatcher?.let { env.messager.addMessageWatcher(messageWatcher) }

    val scopeElements =
        elementsByAnnotation[Scope::class.qualifiedName]
            ?.filterIsInstance<XTypeElement>()
            ?.mapNotNull { it }
            ?.toList()
            .orEmpty()
    val initialScopeClasses = scopeElements.map { CompilerClass(env, it.type) }
    if (initialScopeClasses.isEmpty()) {
      return emptySet()
    } else {
      initialScopeNames += initialScopeClasses.map { it.qualifiedName }
    }

    val graph = ResolvedGraph.create(initialScopeClasses)
    graphSetter(graph)

    val graphErrors = graph.errors
    val filteredGraphErrors =
        graphErrors.filterNot {
          when (it) {
            is AlreadySatisfiedError ->
                (it.existingSources.firstOrNull()?.type?.type as? CompilerType)?.mirror?.isError()
                    ?: false
            is DependencyCycleError ->
                it.path.any { (it.type.type as? CompilerType)?.mirror?.isError() ?: false }
            else -> false
          }
        }
    if (filteredGraphErrors.isNotEmpty()) {
      val errorMessage = ErrorMessage.toString(filteredGraphErrors)
      env.messager.printMessage(Diagnostic.Kind.ERROR, errorMessage)
      return emptySet()
    }

    val mode: OutputMode? =
        try {
          OutputMode.valueOf(env.options[OPTION_MODE]?.uppercase() ?: "")
        } catch (ignore: IllegalArgumentException) {
          if (env.backend == XProcessingEnv.Backend.KSP) OutputMode.KOTLIN else null
        }

    createdScopeNames += CodeGenerator.generate(env, graph, mode)
    if (createdScopeNames.size < initialScopeNames.size) {
      val missingScopeNames = HashSet(initialScopeNames).apply { removeAll(createdScopeNames) }
      env.messager.printMessage(
          Diagnostic.Kind.ERROR,
          """
      Not all scopes were generated.
      Expected: ${initialScopeNames.sorted().joinToString(", ")}
      Created:  ${createdScopeNames.sorted().joinToString(", ")}
      Missing: ${missingScopeNames.sorted().joinToString(", ")}
        """.trimIndent())
      return emptySet()
    }

    return emptySet()
  }
}
