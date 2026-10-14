/*
 * Copyright (c) 2026 Uber Technologies, Inc.
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
package testcases.KT014_runtime_selectable_generic_properties

import motif.Creatable

@motif.Scope(cachingStrategy = motif.CachingStrategy.RUNTIME_SELECTABLE)
interface Scope : Creatable<Scope.Dependencies>, GenericParent<String> {
  @motif.Objects
  abstract class Objects {
    fun optional(): Optional<String> = Optional("value")

    fun nested(): List<List<String?>> = listOf(listOf("first", null))

    fun nullableOptional(): Optional<Int> = Optional(42)
  }

  interface Dependencies
}
