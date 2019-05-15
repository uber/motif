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

import motif.ast.IrType
import motif.models.ChildMethod
import motif.models.Scope

class Child(val parent: Scope, val method: ChildMethod, val scope: Scope)

/**
 * Graph of [Scopes][Scope] as defined by [Scope.childMethods]. Throws an [IllegalStateException] if any of a
 * Scope's child Scopes does not exist in the initial list of Scopes.
 */
internal class ScopeGraph private constructor(val scopes: List<Scope>) {

    private val scopeMap: Map<IrType, Scope> = scopes.associateBy { it.clazz.type }
    private val children: Map<Scope, List<Child>> = scopes.associate { scope -> scope to createChildren(scope) }
    private val parents: Map<Scope, List<Child>> = {
        val mapping = children.values.flatten().groupBy { it.scope }
        scopes.associateWith { mapping[it] ?: emptyList() }
    }()

    val roots: List<Scope> = parents.filter { it.value.isEmpty() }.map { it.key }

    val scopeCycleError: ScopeCycleError? = calculateCycle()

    fun getChildren(scope: Scope): List<Child> {
        return children[scope] ?: throw NullPointerException("Scope not found: ${scope.qualifiedName}")
    }

    private fun createChildren(scope: Scope): List<Child> {
        return scope.childMethods.map { method ->
            val childScope = scopeMap[method.childScopeClass.type]
                    ?: throw IllegalStateException("Scope not found: ${scope.qualifiedName}")
            Child(scope, method, childScope)
        }
    }

    private fun calculateCycle(): ScopeCycleError? {
        // Sort for stable tests
        val sortedScopes = scopes.sortedBy { it.qualifiedName }
        val cycle = Cycle.find(sortedScopes) { scope -> getChildren(scope).map { it.scope } } ?: return null
        return ScopeCycleError(cycle.path)
    }

    companion object {

        fun create(scopes: List<Scope>): ScopeGraph {
            return ScopeGraph(scopes)
        }
    }
}
