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
package motif.models.motif.dependencies

import motif.ast.IrType

class RequiredDependencies(val list: List<RequiredDependency>) {

    val scopeToDependencies: Map<IrType, List<Dependency>> by lazy {
        list.flatMap { requiredDependency ->
            requiredDependency.consumingScopes.map { scopeType ->
                Pair(scopeType, requiredDependency.dependency)
            }
        }.groupBy({ it.first }) { it.second }
    }

    private val map: Map<Dependency, RequiredDependency> by lazy {
        list.associateBy { it.dependency }
    }

    operator fun get(dependency: Dependency): RequiredDependency? {
        return map[dependency]
    }

    operator fun minus(dependencyList: List<Dependency>): RequiredDependencies {
        return RequiredDependencies((map - dependencyList).values.toList())
    }

    operator fun plus(requiredDependencies: RequiredDependencies): RequiredDependencies {
        val newRequiredDependencies = map.keys.plus(requiredDependencies.map.keys).toSet().map { dependency ->
            val isTransitive = isTransitive(dependency) || requiredDependencies.isTransitive(dependency)
            val consumingScopes = consumingScopes(dependency) + requiredDependencies.consumingScopes(dependency)
            RequiredDependency(dependency, isTransitive, consumingScopes)
        }
        return RequiredDependencies(newRequiredDependencies)
    }

    private fun isTransitive(dependency: Dependency): Boolean {
        return map[dependency]?.transitive ?: false
    }

    private fun consumingScopes(dependency: Dependency): Set<IrType> {
        return map[dependency]?.consumingScopes ?: setOf()
    }

    override fun toString(): String {
        return "${map.values}"
    }
}