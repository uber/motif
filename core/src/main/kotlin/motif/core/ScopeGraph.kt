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
import motif.models.ErrorScope
import motif.models.ParsingError
import motif.models.Scope

class ScopeEdge(val parent: Scope, val child: Scope, val method: ChildMethod)

/**
 * Graph of [Scopes] [Scope] as defined by [Scope.childMethods]. Throws an [IllegalStateException]
 * if any of a Scope's childEdge Scopes does not exist in the initial list of Scopes.
 */
internal class ScopeGraph private constructor(val scopes: List<Scope>) {

  private val scopeMap: Map<IrType, Scope> = scopes.associateBy { it.clazz.type }
  private val childEdges: Map<Scope, List<ScopeEdge>> =
      scopes.associate { scope -> scope to createChildren(scope) }
  private val parentEdges: Map<Scope, List<ScopeEdge>> =
      {
        val mapping = childEdges.values.flatten().groupBy { it.child }
        scopes.associateWith { mapping[it] ?: emptyList() }
      }()

  val roots: List<Scope> = parentEdges.filter { it.value.isEmpty() }.map { it.key }

  val scopeCycleError: ScopeCycleError? = calculateCycle()

  val parsingErrors: List<ParsingError> =
      scopes.filterIsInstance<ErrorScope>().map { it.parsingError }

  fun getChildEdges(scope: Scope): List<ScopeEdge> =
      childEdges[scope] ?: throw NullPointerException("Scope not found: ${scope.qualifiedName}")

  fun getParentEdges(scope: Scope): List<ScopeEdge> =
      parentEdges[scope] ?: throw NullPointerException("Scope not found: ${scope.qualifiedName}")

  fun getScope(scopeType: IrType): Scope? = scopeMap[scopeType]

  private fun createChildren(scope: Scope): List<ScopeEdge> =
      scope.childMethods.map { method ->
        val childScope =
            getScope(method.childScopeClass.type)
                ?: throw IllegalStateException("Scope not found: ${scope.qualifiedName}")
        ScopeEdge(scope, childScope, method)
      }

  private fun calculateCycle(): ScopeCycleError? {
    // Sort for stable tests
    val sortedScopes = scopes.sortedBy { it.qualifiedName }
    val cycle =
        Cycle.find(sortedScopes) { scope -> getChildEdges(scope).map { it.child } } ?: return null
    return ScopeCycleError(cycle.path)
  }

  companion object {

    fun create(scopes: List<Scope>): ScopeGraph = ScopeGraph(scopes)
  }
}
