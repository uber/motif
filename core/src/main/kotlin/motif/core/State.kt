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

import motif.models.*

private typealias SetMultiMap<K, V> = MutableMap<K, LinkedHashSet<V>>

private fun <K,V> setMultiMap(): SetMultiMap<K,V> {
    return LinkedHashMap()
}

/**
 * Carries state through the ResolvedGraph creation logic.
 */
internal class State(
        val sinkToSources: SetMultiMap<Sink, Source> = setMultiMap(),
        val sourceToSinks: SetMultiMap<Source, Sink> = setMultiMap(),
        val unsatisfied: MutableSet<Sink> = mutableSetOf(),
        val errors: MutableList<MotifError> = mutableListOf(),
        private val sinks: SetMultiMap<Type, Sink> = setMultiMap(),
        private val exposeNeeded: MutableSet<Sink> = mutableSetOf(),
        private val visibleSinks: SetMultiMap<Sink, Source> = setMultiMap()) {

    private val edges = LinkedHashMap<Source, List<Sink>>()

    fun addSinks(sink: Iterable<Sink>) {
        sink.forEach(this::addSink)
    }

    fun addSink(sink: Sink) {
        if (sinkToSources.containsKey(sink)) {
            throw IllegalStateException("Sink already added.")
        }

        sinkToSources[sink] = LinkedHashSet()
        visibleSinks[sink] = LinkedHashSet()
        sinks.computeIfAbsent(sink.type) { LinkedHashSet() }.add(sink)
        unsatisfied.add(sink)
    }

    fun addSources(sources: Iterable<Source>) {
        sources.forEach(this::addSource)
    }

    fun addSource(source: Source) {
        if (sourceToSinks.containsKey(source)) {
            throw IllegalStateException("Source already added.")
        }

        sourceToSinks[source] = LinkedHashSet()

        val matchingSinks: Set<Sink> = sinks[source.type] ?: LinkedHashSet()

        matchingSinks.forEach { matchingSink ->
            satisfy(matchingSink, source)
        }
    }

    fun setDependencies(scope: Scope, dependencies: Dependencies) {
        val declaredTypes: Set<Type> = dependencies.types.toSet()
        val requiredButNotDeclared: List<Sink> = unsatisfied.filter { !declaredTypes.contains(it.type) }
        requiredButNotDeclared.forEach {
            errors.add(UnsatisfiedDependencyError(scope, it))
            unsatisfied.remove(it)
        }

        val requiredTypes: Set<Type> = unsatisfied.map { it.type }.toSet()
        val declaredButNotRequired: List<Dependencies.Method> = dependencies.methods.filter { !requiredTypes.contains(it.returnType) }
        declaredButNotRequired.forEach {
            errors.add(UnusedDependencyError(it))
        }

        val visibleButNotDeclared = visibleSinks.keys.filter { !declaredTypes.contains(it.type) }
        visibleButNotDeclared.forEach { visibleSinks.remove(it) }
    }

    fun addEdges(source: Source, sinks: List<Sink>) {
        edges[source] = sinks
    }

    fun checkCycle() {
        Cycle.find(edges.keys) { source ->
            edges.getOrDefault(source, emptyList())
                    .flatMap { sink -> sinkToSources.getOrDefault(sink, LinkedHashSet()) }
        }?.let { cycle ->
            errors.add(DependencyCycleError(cycle.path))
        }
    }

    fun requireExpose() {
        exposeNeeded.addAll(visibleSinks.keys)
    }

    fun copy(): State {
        return State(
                sinkToSources.copy(),
                sourceToSinks.copy(),
                unsatisfied.toMutableSet(),
                errors.toMutableList(),
                sinks.copy(),
                exposeNeeded.toMutableSet(),
                visibleSinks.copy())
    }

    private fun satisfy(sink: Sink, source: Source) {
        if (!visibleSinks.contains(sink)) return

        val visibleSources = visibleSinks.getValue(sink)

        val alreadySatisfied = visibleSources.isNotEmpty()
        val exposeConditionMet = !exposeNeeded.contains(sink) || source.isExposed

        if (!alreadySatisfied && !exposeConditionMet) {
            errors.add(UnexposedSourceError(source, sink))
            return
        }

        if (alreadySatisfied && exposeConditionMet) {
            errors.add(AlreadySatisfiedError(source.scope, source, visibleSources.toList()))
        }

        if (source.isOverriding) {
            visibleSinks.remove(sink)
        }

        sinkToSources.getValue(sink).add(source)
        sourceToSinks.getValue(source).add(sink)
        visibleSources.add(source)
        unsatisfied.remove(sink)
    }

    companion object {

        fun merge(states: List<State>): State {
            return State(
                    states.map { it.sinkToSources }.merge(),
                    states.map { it.sourceToSinks }.merge(),
                    states.map { it.unsatisfied }.merge(),
                    states.map { it.errors }.merge(),
                    states.map { it.sinks }.merge(),
                    states.map { it.exposeNeeded }.merge(),
                    states.map { it.visibleSinks }.merge())
        }
     }
}

private fun <K,V> SetMultiMap<K, V>.copy(): SetMultiMap<K, V> {
    val map = setMultiMap<K, V>()
    forEach { k, set ->
        map[k] = LinkedHashSet(set)
    }
    return map
}

private fun <T> List<MutableSet<T>>.merge() = merge(::mutableSetOf)
private fun <T> List<MutableList<T>>.merge() = merge(::mutableListOf)

private fun <C : MutableCollection<T>, T> List<C>.merge(newCollection: () -> C): C {
    val collection = newCollection()
    forEach { collection.addAll(it) }
    return collection
}

private fun <K,V> List<SetMultiMap<K, V>>.merge(): SetMultiMap<K, V> {
    val map = setMultiMap<K,V>()
    forEach { other ->
        other.forEach { k, v ->
            map.computeIfAbsent(k) { LinkedHashSet() }.addAll(v)
        }
    }
    return map
}
