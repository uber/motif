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
package testcases.KT011_baseline_with_lock_selectable_kotlin;

import static com.google.common.truth.Truth.assertThat;

import motif.MotifRuntimeConfig;

public class Test {

    public static void run() {
        // Test with per-dependency locks disabled (synchronized(this))
        MotifRuntimeConfig.usePerDependencyLock = false;
        Scope scope1 = new ScopeImpl();

        String string1a = scope1.fooString();
        String string1b = scope1.fooString();
        assertThat(string1a).isSameInstanceAs(string1b);

        Integer int1a = scope1.fooInt();
        Integer int1b = scope1.fooInt();
        assertThat(int1a).isSameInstanceAs(int1b);

        Object obj1a = scope1.fooObject();
        Object obj1b = scope1.fooObject();
        assertThat(obj1a).isSameInstanceAs(obj1b);

        // Test with per-dependency locks enabled
        MotifRuntimeConfig.usePerDependencyLock = true;
        Scope scope2 = new ScopeImpl();

        String string2a = scope2.fooString();
        String string2b = scope2.fooString();
        assertThat(string2a).isSameInstanceAs(string2b);

        Integer int2a = scope2.fooInt();
        Integer int2b = scope2.fooInt();
        assertThat(int2a).isSameInstanceAs(int2b);

        Object obj2a = scope2.fooObject();
        Object obj2b = scope2.fooObject();
        assertThat(obj2a).isSameInstanceAs(obj2b);

        // Reset to default
        MotifRuntimeConfig.usePerDependencyLock = false;
    }
}
