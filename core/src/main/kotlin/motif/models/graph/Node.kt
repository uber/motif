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

import motif.models.errors.NotExposedDynamicError
import motif.models.errors.NotExposedError
import motif.models.motif.ScopeClass
import motif.models.motif.dependencies.*
import motif.models.motif.objects.FactoryMethod

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

    val notExposedErrors: List<NotExposedError> by lazy {
        childRequiredDependencies.list
                .mapNotNull { requiredDependency ->
                    val factoryMethod = scopeClass.notExposed[requiredDependency.dependency] ?: return@mapNotNull null
                    NotExposedError(scopeClass, factoryMethod, requiredDependency)
                }
    }

    val notExposedDynamicErrors: List<NotExposedDynamicError> by lazy {
        scopeChildren
                .flatMap { child ->
                    val childRequiredDependencies: RequiredDependencies = child.node.requiredDependencies
                    val dynamicDependencies: Map<Dependency, DynamicDependency> = child.method.dynamicDependencies.associateBy { it.dependency }
                    childRequiredDependencies.list
                            .filter {
                                val dynamicDependency = dynamicDependencies[it.dependency]
                                if (dynamicDependency == null) {
                                    false
                                } else {
                                    it.transitive && !dynamicDependency.isExposed
                                }
                            }
                            .map { notExposedDependency ->
                                NotExposedDynamicError(scopeClass, child.method, notExposedDependency)
                            }
                }
    }

    val childRequiredDependencies: RequiredDependencies by lazy {
        scopeChildren
                .map { child ->
                    val childRequiredDependencies: RequiredDependencies = child.node.requiredDependencies
                    val dynamicDependencies = child.method.dynamicDependencies
                    // Similar to the logic in Node.requiredDependencies, this looks incorrect at first glance since
                    // transitive dependencies should only be satisfied by dynamic dependencies annotated with @Expose.
                    // However, we catch these cases in Node.notExposedDynamicErrors.
                    childRequiredDependencies - dynamicDependencies.map { it.dependency }
                }
                .map { it.toTransitive() }
                .merge()
    }

    val requiredDependencies: RequiredDependencies by lazy {
        // This looks like a bug at first since we are seemingly satisfying child required dependencies with
        // all dependencies provided by this scope, when we should instead only be able to satisfy child
        // required dependencies if the provided dependency is @Exposed. However, in cases where we're providing
        // a non-@Exposed dependency that is required by a child (or descendant), we'll surface a NotExposedError.
        // In this case, compilation will fail but we still want an accurate picture of the rest of the graph, which
        // is why we also allow non-@Exposed dependencies to satisfy child required dependencies here.
        val dependencies = childRequiredDependencies - scopeClass.provided + scopeClass.selfRequiredDependencies
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
                DuplicateFactoryMethod(
                        factoryMethod,
                        (visibleFactoryMethodList - factoryMethod).map { it.scopeType }.toSet())
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
            requiredDependencies[it] ?: RequiredDependency(it, false, setOf(scopeClass.ir.type))
        }
        return RequiredDependencies(list)
    }

    private fun RequiredDependencies.toTransitive(): RequiredDependencies {
        return RequiredDependencies(list.map { RequiredDependency(it.dependency, true, it.consumingScopes) })
    }
}