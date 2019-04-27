/*
 * Copyright (c) 2018 Uber Technologies, Inc.
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

interface ResolvedGraph {

    val scopes: List<Scope>

    val errors: List<MotifError>

    fun getChildren(scope: Scope): List<Child>

    fun getChildUnsatisfied(child: Child): List<Sink>

    fun getUnsatisfied(scope: Scope): List<Sink>

    fun getSources(sink: Sink): List<Source>

    companion object {

        fun create(initialScopeClasses: List<IrClass>): ResolvedGraph {
            val scopes = try {
                Scope.fromClasses(initialScopeClasses)
            } catch (e: ParsingError) {
                return ErrorGraph(e)
            }
            val scopeGraph = ScopeGraph.create(scopes)
            return ResolvedGraphFactory(scopeGraph).create()
        }
    }
}

class MissingDependency(val top: Scope, val sink: Sink)

class UnusedDependency(val method: Dependencies.Method)

class UnexposedSource(val source: Source, val sink: Sink)

class AlreadySatisfied(val scope: Scope, val source: Source, val existingSources: List<Source>)

class DependencyCycle(val path: List<Node>)

private class ErrorGraph(error: MotifError) : ResolvedGraph {

    override val scopes = listOf<Scope>()
    override val errors = listOf(error)
    override fun getChildren(scope: Scope) = emptyList<Child>()
    override fun getChildUnsatisfied(child: Child) = listOf<Sink>()
    override fun getUnsatisfied(scope: Scope) = emptyList<Sink>()
    override fun getSources(sink: Sink) = emptyList<Source>()
}

private class ResolvedGraphImpl(
        private val scopeGraph: ScopeGraph,
        private val sinks: Sinks,
        private val scopeSinks: Map<Scope, Sinks>,
        private val childSinks: Map<Child, Sinks>,
        private val missingDependencies: List<MissingDependency>,
        private val unusedDependencies: List<UnusedDependency>,
        private val unexposedSources: List<UnexposedSource>,
        private val alreadySatisfied: List<AlreadySatisfied>,
        private val dependencyCycles: List<DependencyCycle>) : ResolvedGraph {

    private val unsatisfiedSinks: Map<Scope, List<Sink>> by lazy {
        scopeSinks.mapValues { (_, sinks) -> sinks.unsatisfiedSinks.toList().sortedBy { it.type } }
    }

    private val childUnsatisfiedSinks: Map<Child, List<Sink>> by lazy {
        childSinks.mapValues { (_, sinks) -> sinks.unsatisfiedSinks.toList().sortedBy { it.type } }
    }

    override val scopes = scopeGraph.scopes

    override val errors: List<MotifError> by lazy {
        missingDependencies.map { MissingDependencyError(it) } +
                unusedDependencies.map { UnusedDependencyError(it) } +
                unexposedSources.map { UnexposedSourceError(it) } +
                alreadySatisfied.map { AlreadySatisfiedError(it) } +
                dependencyCycles.map { DependencyCycleError(it) }
    }

    override fun getChildren(scope: Scope): List<Child> {
        return scopeGraph.getChildren(scope)
    }

    override fun getChildUnsatisfied(child: Child): List<Sink> {
        return childUnsatisfiedSinks.getValue(child)
    }

    override fun getUnsatisfied(scope: Scope): List<Sink> {
        return unsatisfiedSinks.getValue(scope)
    }

    override fun getSources(sink: Sink): List<Source> {
        return sinks.getSources(sink)
    }
}

private class ResolvedGraphFactory(private val scopeGraph: ScopeGraph) {

    private val resolvedScopes = mutableMapOf<Scope, ResolvedScope>()

    fun create(): ResolvedGraph {
        scopeGraph.cycle?.let { return ErrorGraph(ScopeCycleError(it)) }

        val resolvedRoots = scopeGraph.roots.map { root -> getResolved(root) }

        val sinks = resolvedRoots.fold(Sinks.EMPTY) { acc, resolved -> acc + resolved.sinks }
        val scopeSinks = resolvedScopes.map { (scope, resolved) -> scope to resolved.sinks }.toMap()

        fun <T> fold(mapping: (ResolvedScope) -> List<T>): List<T> {
            return resolvedScopes.values.fold(emptyList()) { acc, resolved -> acc + mapping(resolved) }
        }

        val missingDependencies = fold { it.missingDependencies }
        val unusedDependencies = fold { it.unusedDependencies }
        val unexposedSources = fold { it.unexposedSources }
        val alreadySatisfied = fold { it.alreadySatisfied }

        val dependencyCycles = resolvedScopes.values.mapNotNull { it.dependencyCycle }

        val childSinks: Map<Child, Sinks> = resolvedScopes.map { it.value.childSinks }.fold(mapOf()) { acc, map -> acc + map }

        return ResolvedGraphImpl(
                scopeGraph,
                sinks,
                scopeSinks,
                childSinks,
                missingDependencies,
                unusedDependencies,
                unexposedSources,
                alreadySatisfied,
                dependencyCycles)
    }

    private fun getResolved(scope: Scope): ResolvedScope {
        return resolvedScopes.computeIfAbsent(scope) { computeResolved(scope) }
    }

    private fun computeResolved(scope: Scope): ResolvedScope {
        val resolvedChildren = scopeGraph.getChildren(scope).map { child ->
            val resolved = getResolved(child.scope)
            ResolvedChild(child, resolved)
        }
        return ResolvedScope.create(scope, resolvedChildren)
    }
}

private class Sinks(
        private val hidden: Map<Sink, List<Source>>,
        private val map: Map<Sink, List<Source>>) {

    val sinks: Set<Sink> by lazy {
        map.keys
    }

    val unsatisfiedSinks: Set<Sink> by lazy {
        map.filterValues { it.isEmpty() }.keys
    }

    fun getSources(sink: Sink): List<Source> {
        return map.getValue(sink)
    }

    fun toBuilder(): Builder {
        val mutableMap = mutableMapOf<Sink, MutableList<Source>>()
        map.forEach { sink, sources ->
            mutableMap[sink] = sources.toMutableList()
        }
        return Builder(hidden.toMutableMap(), mutableMap)
    }

    operator fun plus(other: Sinks): Sinks {
        return Sinks(hidden + other.hidden, map + other.map)
    }

    private operator fun Map<Sink, List<Source>>.plus(other: Map<Sink, List<Source>>): Map<Sink, List<Source>> {
        val mutableMap = toMutableMap()
        other.forEach { sink, sources ->
            mutableMap.merge(sink, sources) { a, b -> a + b }
        }
        return mutableMap
    }

    class Builder(
            private val hidden: MutableMap<Sink, List<Source>>,
            private val map: MutableMap<Sink, MutableList<Source>>) {

        fun getSources(sink: Sink): List<Source> {
            return map.getValue(sink)
        }

        fun put(sink: Sink, source: Source) {
            map.getValue(sink).add(source)
        }

        fun hide(sink: Sink) {
            val sources = map.remove(sink)!!
            hidden[sink] = sources
        }

        fun build(): Sinks {
            return Sinks(hidden, map)
        }
    }

    companion object {

        val EMPTY = Sinks(emptyMap(), emptyMap())

        fun fromSinks(sinks: List<Sink>): Sinks {
            return Sinks(mapOf(), sinks.associateWith { listOf<Source>() })
        }
    }
}

private class ResolvedChild(val child: Child, val resolved: ResolvedScope) {

    val sinks: Sinks = resolved.sinks
}

private class ResolvedScope(
        val scope: Scope,
        val sinks: Sinks,
        val childSinks: Map<Child, Sinks>,
        val missingDependencies: List<MissingDependency>,
        val unusedDependencies: List<UnusedDependency>,
        val unexposedSources: List<UnexposedSource>,
        val alreadySatisfied: List<AlreadySatisfied>,
        val dependencyCycle: DependencyCycle?) {

    companion object {

        fun create(scope: Scope, resolvedChildren: List<ResolvedChild>): ResolvedScope {
            return ResolvedScopeFactory(scope, resolvedChildren).create()
        }
    }
}

private class ResolvedScopeFactory(
        private val scope: Scope,
        private val resolvedChildren: List<ResolvedChild>) {

    private val nodeEdges = mutableMapOf<Node, MutableList<Node>>()

    private val missingDependencies = mutableListOf<MissingDependency>()
    private val unusedDependencies = mutableListOf<UnusedDependency>()
    private val unexposedSources = mutableListOf<UnexposedSource>()
    private val alreadySatisfied = mutableListOf<AlreadySatisfied>()

    fun create(): ResolvedScope {
        val scopeNodes = nodes(scope)
        var scopeSinks = Sinks.fromSinks(scopeNodes.mapNotNull { it as? Sink })
        val scopeSources = scopeNodes.mapNotNull { it as? Source }
        scopeSinks = scopeSinks.satisfy(scopeSources)

        val childSinks = resolvedChildren.associateBy(
                keySelector = { it.child },
                valueTransform = { childSinks(it) })

        val resolvedChildSinks = childSinks.values.map {
            it.satisfy(scopeSources) { source, _ -> source.isExposed }
        }

        var resolvedSinks = scopeSinks + resolvedChildSinks.fold(Sinks.EMPTY) { acc, sinks -> acc + sinks }

        scope.dependencies?.let { dependencies ->
            resolvedSinks = resolvedSinks.restrict(dependencies)
        }

        return ResolvedScope(
                scope,
                resolvedSinks,
                childSinks,
                missingDependencies.toList(),
                unusedDependencies.toList(),
                unexposedSources.toList(),
                alreadySatisfied.toList(),
                calculateDependencyCycle())
    }

    private fun calculateDependencyCycle(): DependencyCycle? {
        val cycle = Cycle.find(nodeEdges.keys) { node -> nodeEdges[node] ?: emptyList() } ?: return null
        return DependencyCycle(cycle.path)
    }

    private fun childSinks(resolvedChild: ResolvedChild): Sinks {
        val parameterSources = resolvedChild.child.method.parameters.map { ChildParameterSource(it) }
        return resolvedChild.sinks.satisfy(parameterSources) { source, sink ->
            sink.scope == resolvedChild.child.scope || source.isExposed
        }
    }

    private fun nodes(scope: Scope): List<Node> {
        val factoryMethodNodes = scope.factoryMethods.flatMap { nodes(it) }
        val accessMethodSinks = scope.accessMethods.map { AccessMethodSink(it) }
        val scopeSource = ScopeSource(scope)
        return factoryMethodNodes + accessMethodSinks + scopeSource
    }

    private fun nodes(factoryMethod: FactoryMethod): List<Node> {
        val factoryMethodSource = FactoryMethodSource(factoryMethod)
        val spreadSources = (factoryMethod.spread?.methods ?: emptyList())
                .map { spreadMethod ->
                    val spreadSource = SpreadSource(spreadMethod)
                    putNodeEdge(spreadSource, factoryMethodSource)
                    spreadSource
                }
        val parameterSinks = factoryMethod.parameters.map { parameter ->
            val parameterSink = FactoryMethodSink(parameter)
            putNodeEdge(factoryMethodSource, parameterSink)
            parameterSink
        }
        return spreadSources + factoryMethodSource + parameterSinks
    }

    private fun putNodeEdge(from: Node, to: Node){
        nodeEdges.computeIfAbsent(from) { mutableListOf() }.add(to)
    }

    private fun Sinks.restrict(dependencies: Dependencies): Sinks {
        val requiredTypes: Map<Type, List<Sink>> = unsatisfiedSinks.groupBy { it.type }
        val declaredTypes: Set<Type> = dependencies.methods.map { it.returnType }.toSet()

        val requiredButNotDeclared: List<Sink> = unsatisfiedSinks.filter { required -> !declaredTypes.contains(required.type) }
        val declaredButNotRequired: List<Dependencies.Method> = dependencies.methods.filter { declared -> !requiredTypes.contains(declared.returnType) }

        requiredButNotDeclared.forEach {
            missingDependencies.add(MissingDependency(scope, it))
        }

        declaredButNotRequired.forEach {
            unusedDependencies.add(UnusedDependency(it))
        }

        val builder = toBuilder()

        sinks.filter { !declaredTypes.contains(it.type) }
                .forEach { builder.hide(it) }

        return builder.build()
    }

    private fun Sinks.satisfy(
            sources: List<Source>,
            exposeCondition: (Source, Sink) -> Boolean = { _, _ -> true}): Sinks {
        val builder = toBuilder()

        sinks.forEach { sink ->
            sources.forEach { source ->
                if (source.type == sink.type) {
                    val existingSources = builder.getSources(sink)
                    if (exposeCondition(source, sink)) {
                        if (existingSources.isNotEmpty()) {
                            alreadySatisfied.add(AlreadySatisfied(scope, source, existingSources.toList()))
                        } else {
                            putNodeEdge(sink, source)
                        }
                        builder.put(sink, source)
                    } else {
                        if (existingSources.isEmpty()) {
                            unexposedSources.add(UnexposedSource(source, sink))
                        }
                    }
                }
            }
        }

        return builder.build()
    }
}
