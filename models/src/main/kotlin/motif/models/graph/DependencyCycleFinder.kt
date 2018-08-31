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

import motif.models.motif.ScopeClass
import motif.models.motif.dependencies.Dependency
import motif.models.motif.objects.FactoryMethod

class DependencyCycleFinder(private val scopeClass: ScopeClass) {

    private val factoryMethods: Map<Dependency, FactoryMethod> = scopeClass.objectsClass
            ?.factoryMethods
            ?.associateBy { it.providedDependency }
            ?: mapOf()

    fun findCycle(): DependencyCycle? {
        factoryMethods.values.forEach {
            findCycle(listOf(), it)?.let { return it }
        }
        return null
    }

    private fun findCycle(visited: List<FactoryMethod>, factoryMethod: FactoryMethod): DependencyCycle? {
        val cycleStartIndex = visited.indexOf(factoryMethod)
        if (cycleStartIndex != -1) {
            return DependencyCycle(scopeClass, visited.subList(cycleStartIndex, visited.size))
        }

        val newVisited = visited + factoryMethod

        factoryMethod.requiredDependencies.list
                .map { it.dependency }
                .mapNotNull { factoryMethods[it] }
                .forEach { findCycle(newVisited, it)?.let { return it } }

        return null
    }
}