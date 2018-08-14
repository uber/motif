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
package motif.ir.source.dependencies

import motif.ir.source.base.Dependency
import motif.ir.source.base.Type

class RequiredDependencies(val list: List<RequiredDependency>) {

    val scopeToDependencies: Map<Type, List<Dependency>> by lazy {
        list.flatMap { annotatedDependency ->
            annotatedDependency.consumingScopes.map { scopeType ->
                Pair(scopeType, annotatedDependency.dependency)
            }
        }.groupBy({ it.first }) { it.second }
    }

    private val map: Map<Dependency, RequiredDependency> by lazy {
        list.associateBy { it.dependency }
    }

    fun toTransitive(): RequiredDependencies {
        return RequiredDependencies(list.map { RequiredDependency(it.dependency, true, it.consumingScopes) })
    }

    /**
     * Dynamic dependencies are internal so they can only satisfy dependencies for the immediate scope into which
     * they are passed.
     */
    fun satisfiedByDynamic(immediateScopeType: Type, dependencyList: List<Dependency>): RequiredDependencies {
        val result = list.mapNotNull { annotatedDependency ->
            when {
                annotatedDependency.transitive -> annotatedDependency - immediateScopeType
                annotatedDependency.dependency in dependencyList -> null
                else -> annotatedDependency
            }
        }
        return RequiredDependencies(result)
    }

    operator fun get(dependency: Dependency): RequiredDependency? {
        return map[dependency]
    }

    operator fun minus(dependencyList: List<Dependency>): RequiredDependencies {
        return RequiredDependencies((map - dependencyList).values.toList())
    }

    operator fun plus(requiredDependencies: RequiredDependencies): RequiredDependencies {
        val annotatedDependencies = map.keys.plus(requiredDependencies.map.keys).toSet().map { dependency ->
            val isTransitive = isTransitive(dependency) || requiredDependencies.isTransitive(dependency)
            val consumingScopes = consumingScopes(dependency) + requiredDependencies.consumingScopes(dependency)
            RequiredDependency(dependency, isTransitive, consumingScopes)
        }
        return RequiredDependencies(annotatedDependencies)
    }

    private fun isTransitive(dependency: Dependency): Boolean {
        return map[dependency]?.transitive ?: false
    }

    private fun consumingScopes(dependency: Dependency): Set<Type> {
        return map[dependency]?.consumingScopes ?: setOf()
    }

    override fun toString(): String {
        return "${map.values}"
    }
}