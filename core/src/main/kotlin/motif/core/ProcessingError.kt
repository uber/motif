/*
 * Copyright (c) 2018-2019 Uber Technologies, Inc.
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
package motif.core

import motif.models.MotifError
import motif.models.Node
import motif.models.Scope
import motif.models.Sink
import motif.models.Source

sealed class ProcessingError : MotifError

class ScopeCycleError(val path: List<Scope>) : ProcessingError()

class UnsatisfiedDependencyError(val top: Scope, val sink: Sink) : ProcessingError()

class DependencyCycleError(val path: List<Node>) : ProcessingError()

class UnexposedSourceError(val source: Source, val sink: Sink) : ProcessingError()

class AlreadySatisfiedError(
    val scope: Scope,
    val source: Source,
    val existingSources: List<Source>
) : ProcessingError()
