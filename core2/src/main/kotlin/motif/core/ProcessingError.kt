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
package motif.core

import motif.models.MotifError

sealed class ProcessingError : MotifError {

    override val humanReadable: String
        get() = TODO()
}

class ScopeCycleError(val cycle: ScopeCycle) : ProcessingError() {

    override val testString: String by lazy {
        cycle.path.joinToString { it.clazz.simpleName }
    }
}

class MissingDependencyError(val missing: MissingDependency) : ProcessingError() {

    override val testString: String by lazy {
        missing.sink.testString
    }
}

class UnusedDependencyError(val unused: UnusedDependency) : ProcessingError() {

    override val testString: String by lazy {
        unused.method.qualifiedName
    }
}

class DependencyCycleError(val cycle: DependencyCycle) : ProcessingError() {

    override val testString by lazy {
        cycle.path.joinToString { it.testString }
    }
}

class UnexposedSourceError(val unexposedSource: UnexposedSource) : ProcessingError() {

    override val testString: String by lazy {
        "${unexposedSource.source.testString}->${unexposedSource.sink.testString}"
    }
}

class AlreadySatisfiedError(val alreadySatisfied: AlreadySatisfied) : ProcessingError() {

    override val testString: String by lazy {
        "${alreadySatisfied.source.testString}: ${alreadySatisfied.existingSources.joinToString { it.testString }}"
    }
}