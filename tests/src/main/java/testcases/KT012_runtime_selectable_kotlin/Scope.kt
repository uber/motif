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
package testcases.KT012_runtime_selectable_kotlin

import motif.Creatable
import motif.CachingStrategy

@motif.Scope(cachingStrategy = CachingStrategy.RUNTIME_SELECTABLE)
interface Scope : Creatable<Scope.Dependencies> {

    // Public accessor methods to test caching
    fun fooString(): String
    fun fooInt(): Int

    @motif.Objects
    abstract class Objects {
        fun fooString(): String = String("foo".toCharArray())

        fun fooInt(): Int = 42
    }

    interface Dependencies
}
