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
package motif.models.graph

import motif.models.errors.*
import motif.ast.IrType
import motif.models.motif.dependencies.RequiredDependencies

class Graph(
        private val nodes: Map<IrType, Node>,
        private val parsingErrors: List<MotifError>,
        private val scopeCycleError: ScopeCycleError?) {

    val scopes: List<Scope> = nodes.map { (_, node) ->
        Scope(node.scopeClass, node.childRequiredDependencies, node.requiredDependencies)
    }

    val errors: MotifErrors by lazy {
        MotifErrors(
                parsingErrors,
                scopeCycleError,
                missingDependenciesError(),
                dependencyCycleErrors(),
                duplicateFactoryMethodsErrors(),
                notExposedErrors(),
                notExposedDynamicErrors())
    }

    private fun missingDependenciesError(): List<MissingDependenciesError> {
        val allMissingDepenendencies: List<RequiredDependencies> = nodes.values.mapNotNull { it.missingDependencies }
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

    private fun dependencyCycleErrors(): List<DependencyCycleError> {
        return nodes.values
                .mapNotNull {
                    it.dependencyCycle
                }
                .map {
                    DependencyCycleError(it.scopeClass, it.cycle)
                }
    }

    private fun duplicateFactoryMethodsErrors(): List<DuplicateFactoryMethodsError> {
        return nodes.values.flatMap { it.duplicateFactoryMethods }
                .map { DuplicateFactoryMethodsError(it.duplicate, it.existing) }
    }

    private fun notExposedErrors(): List<NotExposedError> {
        return nodes.values.flatMap { it.notExposedErrors }
    }

    private fun notExposedDynamicErrors(): List<NotExposedDynamicError> {
        return nodes.values.flatMap { it.notExposedDynamicErrors }
    }

    fun getDependencies(scopeType: IrType): RequiredDependencies? {
        return nodes[scopeType]?.requiredDependencies
    }
}