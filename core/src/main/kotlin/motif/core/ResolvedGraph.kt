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
import motif.models.*

/**
 * Full representation of the Scope and dependency graph.
 */
interface ResolvedGraph {

    val roots: List<Scope>

    val scopes: List<Scope>

    val scopeFactories: List<ScopeFactory>

    val errors: List<MotifError>

    fun getScope(scopeClass: IrClass): Scope?

    fun getChildEdges(scope: Scope): Iterable<ScopeEdge>

    fun getChildUnsatisfied(scopeEdge: ScopeEdge): Iterable<Sink>

    fun getUnsatisfied(scope: Scope): Iterable<Sink>

    fun getSources(sink: Sink): Iterable<Source>

    companion object {

        fun create(initialScopeClasses: List<IrClass>, scopeFactoryClasses: List<IrClass>): ResolvedGraph {
            val scopeGraph = try {
                val scopeFactories = scopeFactoryClasses.map { ScopeFactory(it) }
                val scopes = Scope.fromClasses(initialScopeClasses + scopeFactories.map { it.scopeClass })
                ScopeGraph.create(scopes, scopeFactories)
            } catch (e: ParsingError) {
                return ErrorGraph(e)
            }
            scopeGraph.scopeCycleError?.let { return ErrorGraph(it) }
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
        val childStates = scopeGraph.getChildEdges(scope).map { childEdge ->
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

        scope.dependencies?.let { state.setDependencies(scope, it) }
        scopeGraph.getFactories(scope).forEach { state.setDependencies(scope, it.dependencies) }

        scope.factoryMethods.forEach { factoryMethod ->
            factoryMethod.sources.forEach { source ->
                state.addEdges(source, factoryMethod.sinks)
            }
        }

        state.checkCycle()

        return state
    }
}

private class ErrorGraph(error: MotifError) : ResolvedGraph {

    override val roots = emptyList<Scope>()
    override val scopes = emptyList<Scope>()
    override val scopeFactories = emptyList<ScopeFactory>()
    override val errors = listOf(error)
    override fun getScope(scopeClass: IrClass) = null
    override fun getChildEdges(scope: Scope) = emptyList<ScopeEdge>()
    override fun getChildUnsatisfied(scopeEdge: ScopeEdge) = emptyList<Sink>()
    override fun getUnsatisfied(scope: Scope) = emptyList<Sink>()
    override fun getSources(sink: Sink) = emptyList<Source>()
}

private class ValidResolvedGraph(
        private val scopeGraph: ScopeGraph,
        private val scopeStates: Map<Scope, State>,
        private val childStates: Map<ScopeEdge, State>,
        private val graphState: State) : ResolvedGraph {

    override val roots = scopeGraph.roots

    override val scopes = scopeGraph.scopes

    override val scopeFactories = scopeGraph.scopeFactories

    override val errors = graphState.errors

    override fun getScope(scopeClass: IrClass) = scopeGraph.getScope(scopeClass)

    override fun getChildEdges(scope: Scope) = scopeGraph.getChildEdges(scope)

    override fun getChildUnsatisfied(scopeEdge: ScopeEdge) = childStates.getValue(scopeEdge).unsatisfied

    override fun getUnsatisfied(scope: Scope) = scopeStates.getValue(scope).unsatisfied

    override fun getSources(sink: Sink) = graphState.sinkToSources.getValue(sink)
}
