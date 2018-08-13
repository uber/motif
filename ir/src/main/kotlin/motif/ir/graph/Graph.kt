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
package motif.ir.graph

import motif.ir.graph.errors.*
import motif.ir.source.base.Type
import motif.ir.source.dependencies.Dependencies

class Graph(
        private val nodes: Map<Type, Node>,
        private val scopeCycleError: ScopeCycleError?) {

    val scopes: List<Scope> = nodes.map { (_, node) ->
        Scope(node.scopeClass, node.childDependencies, node.dependencies)
    }

    val validationErrors: GraphValidationErrors by lazy {
        GraphValidationErrors(
                scopeCycleError,
                missingDependenciesError(),
                dependencyCycleError(),
                duplicateFactoryMethodsError())
    }

    private fun missingDependenciesError(): List<MissingDependenciesError> {
        val allMissingDepenendencies: List<Dependencies> = nodes.values.mapNotNull { it.missingDependencies }
        return if (allMissingDepenendencies.isEmpty()) {
            listOf()
        }  else {
            val missing = allMissingDepenendencies.reduce { acc, dependencies -> acc + dependencies }
            missing.scopeToDependencies.map { (scopeType, missing) ->
                val scopeNode = nodes[scopeType] ?: throw IllegalStateException()
                MissingDependenciesError(scopeNode, missing)
            }
        }
    }

    private fun dependencyCycleError(): DependencyCycleError? {
        val cycles = nodes.values.mapNotNull { it.dependencyCycle }
        return if (cycles.isEmpty()) {
            null
        } else {
            DependencyCycleError(cycles)
        }
    }

    private fun duplicateFactoryMethodsError(): DuplicateFactoryMethodsError? {
        val duplicates = nodes.values.flatMap { it.duplicateFactoryMethods }
        return if (duplicates.isEmpty()) {
            null
        } else {
            DuplicateFactoryMethodsError(duplicates)
        }
    }

    fun getDependencies(scopeType: Type): Dependencies? {
        return nodes[scopeType]?.dependencies
    }
}