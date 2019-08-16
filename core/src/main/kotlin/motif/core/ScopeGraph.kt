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
import motif.models.ChildMethod
import motif.models.Scope
import motif.models.ScopeFactory

class ScopeEdge(val parent: Scope, val child: Scope, val method: ChildMethod)

/**
 * Graph of [Scopes][Scope] as defined by [Scope.childMethods]. Throws an [IllegalStateException] if any of a
 * Scope's childEdge Scopes does not exist in the initial list of Scopes.
 */
internal class ScopeGraph private constructor(val scopes: List<Scope>, val scopeFactories: List<ScopeFactory>) {

    private val scopeMap: Map<IrType, Scope> = scopes.associateBy { it.clazz.type }
    private val childEdges: Map<Scope, List<ScopeEdge>> = scopes.associate { scope -> scope to createChildren(scope) }
    private val parentEdges: Map<Scope, List<ScopeEdge>> = {
        val mapping = childEdges.values.flatten().groupBy { it.child }
        scopes.associateWith { mapping[it] ?: emptyList() }
    }()
    private val scopeFactoryMap: Map<Scope, List<ScopeFactory>> = scopeFactories.groupBy {
        scopeMap.getValue(it.scopeClass.type)
    }

    val roots: List<Scope> = parentEdges.filter { it.value.isEmpty() }.map { it.key }

    val scopeCycleError: ScopeCycleError? = calculateCycle()

    fun getChildEdges(scope: Scope): List<ScopeEdge> {
        return childEdges[scope] ?: throw NullPointerException("Scope not found: ${scope.qualifiedName}")
    }

    fun getScope(scopeClass: IrClass): Scope? {
        return scopeMap[scopeClass.type]
    }

    fun getFactories(scope: Scope): List<ScopeFactory> {
        return scopeFactoryMap.getOrDefault(scope, emptyList())
    }

    private fun createChildren(scope: Scope): List<ScopeEdge> {
        return scope.childMethods.map { method ->
            val childScope = getScope(method.childScopeClass)
                    ?: throw IllegalStateException("Scope not found: ${scope.qualifiedName}")
            ScopeEdge(scope, childScope, method)
        }
    }

    private fun calculateCycle(): ScopeCycleError? {
        // Sort for stable tests
        val sortedScopes = scopes.sortedBy { it.qualifiedName }
        val cycle = Cycle.find(sortedScopes) { scope -> getChildEdges(scope).map { it.child } } ?: return null
        return ScopeCycleError(cycle.path)
    }

    companion object {

        fun create(scopes: List<Scope>, scopeFactories: List<ScopeFactory>): ScopeGraph {
            return ScopeGraph(scopes, scopeFactories)
        }
    }
}
