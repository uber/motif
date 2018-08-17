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
package motif.ir.graph.errors

import motif.ir.graph.DependencyCycle
import motif.ir.graph.DuplicateFactoryMethod
import motif.ir.graph.Node
import motif.ir.source.ScopeClass
import motif.ir.source.base.Dependency
import motif.ir.source.base.Type
import motif.ir.source.dependencies.RequiredDependency
import motif.ir.source.objects.FactoryMethod

sealed class GraphError

class DependencyCycleError(val cycles: List<DependencyCycle>) : GraphError()

class DuplicateFactoryMethodsError(val duplicates: List<DuplicateFactoryMethod>) : GraphError()

class MissingDependenciesError(
        val requiredBy: Node,
        val dependencies: List<Dependency>) : GraphError()

class ScopeCycleError(val cycle: List<Type>) : GraphError()

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