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
package motif.core

import motif.ast.IrClass
import motif.ast.IrType
import motif.models.MotifError
import motif.models.Scope
import motif.models.Sink
import motif.models.Source
import motif.models.Type

/** Full representation of the Scope and dependency graph. */
interface ResolvedGraph {

  val roots: List<Scope>

  val scopes: List<Scope>

  val errors: List<MotifError>

  fun getScope(scopeType: IrType): Scope?

  fun getChildEdges(scope: Scope): Iterable<ScopeEdge>

  fun getParentEdges(scope: Scope): Iterable<ScopeEdge>

  fun getChildUnsatisfied(scopeEdge: ScopeEdge): Iterable<Sink>

  fun getUnsatisfied(scope: Scope): Map<Type, List<Sink>>

  fun getSources(scope: Scope): Iterable<Source>

  fun getSinks(scope: Scope): Iterable<Sink>

  fun getSinks(type: Type): Iterable<Sink>

  fun getSinks(irType: IrType): Iterable<Sink>

  fun getSources(irType: IrType): Iterable<Source>

  fun getProviders(sink: Sink): Iterable<Source>

  fun getConsumers(source: Source): Iterable<Sink>

  fun getRequired(source: Source): Iterable<Sink>

  companion object {

    fun create(initialScopeClasses: List<IrClass>): ResolvedGraph {
//      if(true) {
//        throw RuntimeException(
//          initialScopeClasses.joinToString {
//            it.annotations.joinToString { annotation ->
//              "Rubin ${annotation.className}"
//            } + "[] ${it.simpleName} () (${it.qualifiedName})"
//          }
//        )
//      }
      val scopes = Scope.fromClasses(initialScopeClasses)
      val scopeGraph = ScopeGraph.create(scopes)
      scopeGraph.scopeCycleError?.let {
        return ErrorGraph(it)
      }
      return ResolvedGraphFactory(scopeGraph).create()
    }
  }
}

private class ResolvedGraphFactory(private val scopeGraph: ScopeGraph) {

  private val scopeStates = mutableMapOf<Scope, State>()
  private val childStates = mutableMapOf<ScopeEdge, State>()

  fun create(): ResolvedGraph {
    val states = scopeGraph.roots.map { getState(it) }
    val state = State.merge(states)
    return ValidResolvedGraph(scopeGraph, scopeStates, childStates, state)
  }

  private fun getState(scope: Scope): State {
    // Not using computeIfAbsent here since it seems to have undefined behavior when modifying
    // the map inside the lambda.
    return scopeStates[scope] ?: createState(scope).apply { scopeStates[scope] = this }
  }

  private fun createState(scope: Scope): State {
    val childStates =
        scopeGraph.getChildEdges(scope).map { childEdge ->
          val childState = getState(childEdge.child).copy()
          childState.addSources(childEdge.method.sources)
          childState.requireExpose()
          childStates[childEdge] = childState
          childState
        }
    val state = State.merge(childStates)

    val factoryMethodSinks = scope.factoryMethods.flatMap { it.sinks }
    state.addSinks(factoryMethodSinks)

    val accessMethodSinks = scope.accessMethods.map { it.sink }
    state.addSinks(accessMethodSinks)

    val factoryMethodSources = scope.factoryMethods.flatMap { it.sources }
    state.addSources(factoryMethodSources)

    val scopeSource = scope.source
    state.addSource(scopeSource)

    scope.dependencies?.let { state.setDependencies(it) }

    scope.factoryMethods.forEach { factoryMethod ->
      factoryMethod.sources.forEach { source -> state.addEdges(source, factoryMethod.sinks) }
    }

    state.checkCycle()

    return state
  }
}

private class ErrorGraph(error: MotifError) : ResolvedGraph {

  override val roots = emptyList<Scope>()
  override val scopes = emptyList<Scope>()
  override val errors = listOf(error)

  override fun getScope(scopeType: IrType) = null

  override fun getChildEdges(scope: Scope) = emptyList<ScopeEdge>()

  override fun getParentEdges(scope: Scope) = emptyList<ScopeEdge>()

  override fun getChildUnsatisfied(scopeEdge: ScopeEdge) = emptyList<Sink>()

  override fun getUnsatisfied(scope: Scope) = emptyMap<Type, List<Sink>>()

  override fun getSources(scope: Scope) = emptyList<Source>()

  override fun getSinks(type: Type) = emptyList<Sink>()

  override fun getSinks(irType: IrType) = emptyList<Sink>()

  override fun getSources(irType: IrType) = emptyList<Source>()

  override fun getSinks(scope: Scope) = emptyList<Sink>()

  override fun getProviders(sink: Sink) = emptyList<Source>()

  override fun getConsumers(source: Source) = emptyList<Sink>()

  override fun getRequired(source: Source) = emptyList<Sink>()
}

private class ValidResolvedGraph(
    private val scopeGraph: ScopeGraph,
    private val scopeStates: Map<Scope, State>,
    private val childStates: Map<ScopeEdge, State>,
    private val graphState: State,
) : ResolvedGraph {

  private val scopeSinks = mutableMapOf<Scope, Set<Sink>>()

  override val roots = scopeGraph.roots

  override val scopes = scopeGraph.scopes

  override val errors = scopeGraph.parsingErrors + graphState.errors

  override fun getScope(scopeType: IrType) = scopeGraph.getScope(scopeType)

  override fun getChildEdges(scope: Scope) = scopeGraph.getChildEdges(scope)

  override fun getParentEdges(scope: Scope) = scopeGraph.getParentEdges(scope)

  override fun getChildUnsatisfied(scopeEdge: ScopeEdge) =
      childStates.getValue(scopeEdge).unsatisfied

  override fun getUnsatisfied(scope: Scope) =
      scopeStates.getValue(scope).unsatisfied.groupBy { it.type }

  override fun getSources(scope: Scope) =
      scopeStates.getValue(scope).sourceToSinks.keys.filter { it.scope == scope }

  override fun getSinks(type: Type) = graphState.sinks[type] ?: emptySet<Sink>()

  override fun getSinks(irType: IrType) = graphState.irTypeToSinks[irType] ?: emptySet<Sink>()

  override fun getSources(irType: IrType) = graphState.irTypeToSources[irType] ?: emptySet<Source>()

  override fun getSinks(scope: Scope) =
      scopeSinks.computeIfAbsent(scope) {
        scopeStates
            .getValue(scope)
            .sinks
            .values
            .flatMap { sinks -> sinks.filter { sink -> sink.scope == scope } }
            .toSet()
      }

  override fun getProviders(sink: Sink) = graphState.sinkToSources.getValue(sink)

  override fun getConsumers(source: Source) = graphState.sourceToSinks.getValue(source)

  override fun getRequired(source: Source) =
      scopeStates.getValue(source.scope).edges[source] ?: emptyList()
}
