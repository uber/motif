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

import com.google.common.truth.Truth.assertThat
import motif.CachingStrategy
import motif.MotifRuntimeConfig

object Test {
  @JvmStatic
  fun run() {
    val originalStrategy = MotifRuntimeConfig.cachingStrategy
    try {
      for (strategy in
          listOf(CachingStrategy.BASELINE_WITH_LOCK_SELECTABLE, CachingStrategy.SMART_CACHE)) {
        MotifRuntimeConfig.cachingStrategy = strategy
        assertProperties(ScopeImpl())
      }
    } finally {
      MotifRuntimeConfig.cachingStrategy = originalStrategy
    }
  }

  private fun assertProperties(scope: Scope) {
    val optional: Optional<String> = scope.optional
    val nested: List<List<String?>> = scope.nested
    val nullableOptional: Optional<Int>? = scope.nullableOptional

    assertThat(optional.value).isEqualTo("value")
    assertThat(scope.optional).isSameInstanceAs(optional)
    assertThat(nested).containsExactly(listOf("first", null))
    assertThat(scope.nested).isSameInstanceAs(nested)
    assertThat(nullableOptional?.value).isEqualTo(42)
  }
}
