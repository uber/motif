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
package motif.models.graph.errors

import motif.models.graph.Node
import motif.models.java.IrType
import motif.models.motif.ScopeClass
import motif.models.motif.dependencies.Dependency
import motif.models.motif.dependencies.RequiredDependency
import motif.models.motif.objects.FactoryMethod

sealed class GraphError

class DependencyCycleError(val scopeClass: ScopeClass, val cycle: List<FactoryMethod>) : GraphError()

class DuplicateFactoryMethodsError(val duplicate: FactoryMethod, val existing: Set<IrType>) : GraphError()

class MissingDependenciesError(
        val requiredBy: Node,
        val dependencies: List<Dependency>) : GraphError()

class ScopeCycleError(val cycle: List<IrType>) : GraphError()

/**
 * Compared to other GraphErrors, it's not as intuitive why NotExposedError needs to exist. We hit this error
 * when an ancestor scope defines a non-@Exposed factory method that provides a dependency required
 * by one of its descendants. Initially, it may seem like a premature failure since that dependency may be satisfied
 * by scopes higher in the graph. However, if a scope higher in the graph exposes a factory method that provides the
 * same type, the lower, non-@Exposed factory method would conflict, causing a DuplicateFactoryMethodsError. Thus,
 * there is no situation where this case is valid so we surface this error.
 */
class NotExposedError(
        val scopeClass: ScopeClass,
        val factoryMethod: FactoryMethod,
        val requiredDependency: RequiredDependency) : GraphError()