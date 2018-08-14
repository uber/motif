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

import motif.ir.source.ScopeClass
import motif.ir.source.base.Dependency
import motif.ir.source.dependencies.RequiredDependency
import motif.ir.source.dependencies.RequiredDependencies
import motif.ir.source.dependencies.ExplicitDependencies
import motif.ir.source.objects.FactoryMethod

class Node(
        val scopeClass: ScopeClass,
        private val scopeChildren: List<ScopeChild>) {

    private var internalMissingDependencies: RequiredDependencies? = null

    val missingDependencies: RequiredDependencies? by lazy {
        requiredDependencies // Ensure dependencies are resolved.
        internalMissingDependencies
    }

    val dependencyCycle: DependencyCycle? by lazy {
        DependencyCycleFinder(scopeClass).findCycle()
    }

    val childRequiredDependencies: RequiredDependencies by lazy {
        scopeChildren
                .map { child ->
                    val childRequiredDependencies: RequiredDependencies = child.node.requiredDependencies
                    val dynamicDependencies = child.method.dynamicDependencies
                    childRequiredDependencies.satisfiedByDynamic(child.method.scope, dynamicDependencies)
                }
                .map { it.toTransitive() }
                .merge()
    }

    val requiredDependencies: RequiredDependencies by lazy {
        val dependencies = childRequiredDependencies - scopeClass.exposed + scopeClass.selfRequiredDependencies
        scopeClass.explicitDependencies?.let { explicitDependencies ->
            val missingDependencies = dependencies - explicitDependencies.dependencies
            if (missingDependencies.list.isNotEmpty()) {
                internalMissingDependencies = missingDependencies
            }
            return@lazy explicitDependencies.override(scopeClass, dependencies)
        }
        dependencies
    }

    private val ancestorFactoryMethods: List<FactoryMethod> by lazy {
        parents.flatMap { it.exposedFactoryMethods }
    }

    private val exposedFactoryMethods: List<FactoryMethod> by lazy {
        scopeClass.factoryMethods.filter { it.isExposed } + ancestorFactoryMethods
    }

    val duplicateFactoryMethods: List<DuplicateFactoryMethod> by lazy {
        val visibleFactoryMethods: Map<Dependency, List<FactoryMethod>> = (scopeClass.factoryMethods + ancestorFactoryMethods)
                .groupBy { it.providedDependency }

        scopeClass.factoryMethods.mapNotNull { factoryMethod ->
            val visibleFactoryMethodList = visibleFactoryMethods[factoryMethod.providedDependency] ?: throw IllegalStateException()
            if (visibleFactoryMethodList.size > 1) {
                DuplicateFactoryMethod(factoryMethod, visibleFactoryMethodList - factoryMethod)
            } else {
                null
            }
        }
    }

    val children: List<Node> = scopeChildren.map { it.node }

    val parents: MutableList<Node> = mutableListOf()

    private fun List<RequiredDependencies>.merge(): RequiredDependencies {
        return when {
            isEmpty() -> RequiredDependencies(listOf())
            size == 1 -> this[0]
            else -> reduce { acc, dependencies -> acc + dependencies }
        }
    }

    private fun ExplicitDependencies.override(scopeClass: ScopeClass, requiredDependencies: RequiredDependencies): RequiredDependencies {
        val list = this.dependencies.map {
            requiredDependencies[it] ?: RequiredDependency(it, false, setOf(scopeClass.type))
        }
        return RequiredDependencies(list)
    }
}