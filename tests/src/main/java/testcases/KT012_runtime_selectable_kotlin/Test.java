/*
 * Copyright (c) 2025 Uber Technologies, Inc.
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
package testcases.KT012_runtime_selectable_kotlin;

import static com.google.common.truth.Truth.assertThat;

import motif.MotifRuntimeConfig;
import motif.CachingStrategy;

public class Test {

    public static void run() {
        // Save original strategy
        CachingStrategy originalStrategy = MotifRuntimeConfig.cachingStrategy;

        // Test with BASELINE_WITH_LOCK_SELECTABLE strategy
        MotifRuntimeConfig.cachingStrategy = CachingStrategy.BASELINE_WITH_LOCK_SELECTABLE;
        Scope scope1 = new ScopeImpl();

        String string1a = scope1.fooString();
        String string1b = scope1.fooString();
        assertThat(string1a).isSameInstanceAs(string1b);

        Integer int1a = scope1.fooInt();
        Integer int1b = scope1.fooInt();
        assertThat(int1a).isSameInstanceAs(int1b);

        // Test with SMART_CACHE strategy
        MotifRuntimeConfig.cachingStrategy = CachingStrategy.SMART_CACHE;
        Scope scope2 = new ScopeImpl();

        String string2a = scope2.fooString();
        String string2b = scope2.fooString();
        assertThat(string2a).isSameInstanceAs(string2b);

        Integer int2a = scope2.fooInt();
        Integer int2b = scope2.fooInt();
        assertThat(int2a).isSameInstanceAs(int2b);

        // Restore original strategy
        MotifRuntimeConfig.cachingStrategy = originalStrategy;
    }
}
