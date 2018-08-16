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
import motif.ir.source.base.Dependency
import motif.ir.source.base.Type

sealed class GraphError

class DependencyCycleError(val cycles: List<DependencyCycle>) : GraphError()

class DuplicateFactoryMethodsError(val duplicates: List<DuplicateFactoryMethod>) : GraphError()

class MissingDependenciesError(
        val requiredBy: Node,
        val dependencies: List<Dependency>) : GraphError()

class ScopeCycleError(val cycle: List<Type>) : GraphError()