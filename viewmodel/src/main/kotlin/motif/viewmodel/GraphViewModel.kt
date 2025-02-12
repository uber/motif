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
package motif.viewmodel

import motif.core.ResolvedGraph
import motif.models.ChildParameterSource
import motif.models.Scope
import motif.models.Sink
import motif.models.Source

class GraphViewModel(val rootScopes: List<ScopeViewModel>) {

  companion object {

    fun create(graph: ResolvedGraph): GraphViewModel = GraphViewModelFactory(graph).create()
  }
}

private class GraphViewModelFactory(private val graph: ResolvedGraph) {

  private val scopeViewModels = mutableMapOf<Scope, ScopeViewModel>()

  fun create(): GraphViewModel {
    val rootScopes = graph.roots.map(this::getScopeViewModel).sortedBy { it.scope.qualifiedName }
    return GraphViewModel(rootScopes)
  }

  private fun getScopeViewModel(scope: Scope): ScopeViewModel =
      scopeViewModels[scope] ?: createScopeViewModel(scope).apply { scopeViewModels[scope] = this }

  private fun createScopeViewModel(scope: Scope): ScopeViewModel {
    val children =
        graph
            .getChildEdges(scope)
            .map { edge -> getScopeViewModel(edge.child) }
            .sortedBy { it.scope.qualifiedName }
    val providedDependencies =
        graph
            .getSources(scope)
            .filter { it !is ChildParameterSource }
            .map(this::createProvidedDependency)
            .sortedBy { it.source.type }
    val requiredDependencies = createRequiredDependencies(scope)

    return ScopeViewModel(scope, children, providedDependencies, requiredDependencies)
  }

  private fun createProvidedDependency(source: Source): ProvidedDependency {
    val consumedBy = graph.getConsumers(source).toList().sortedBy { it.type }
    val requiredDependencies = graph.getRequired(source).map(this::createRequiredDependency)
    return ProvidedDependency(source, consumedBy, requiredDependencies)
  }

  private fun createRequiredDependency(sink: Sink): RequiredDependency {
    val providedBy = graph.getProviders(sink).toList().sortedBy { it.type }
    return RequiredDependency(sink.type, providedBy, listOf(sink))
  }

  private fun createRequiredDependencies(scope: Scope): List<RequiredDependency> {
    val unsatisfied = graph.getUnsatisfied(scope)

    return unsatisfied
        .map { (type, sinks) ->
          var sources: Iterable<Source>? = null
          for (sink in sinks) {
            val prevSources = sources
            sources = graph.getProviders(sink)
            if (prevSources != null && sources != prevSources) {
              throw IllegalStateException(
                  "Inconsistent sources for sinks of the same type: $scope, $type",
              )
            }
          }

          if (sources == null) {
            throw IllegalStateException("No sink found for given Type: $type")
          }

          RequiredDependency(type, sources.toList().sortedBy { it.type }, sinks)
        }
        .sortedBy { it.type }
  }
}
