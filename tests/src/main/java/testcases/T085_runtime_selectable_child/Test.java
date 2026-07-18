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
package testcases.T085_runtime_selectable_child;

import static com.google.common.truth.Truth.assertThat;

import motif.MotifRuntimeConfig;
import motif.CachingStrategy;

public class Test {

    public static void run() {
        CachingStrategy original = MotifRuntimeConfig.cachingStrategy;
        try {
            // given the control strategy, the wrapper delegates parent and child methods
            MotifRuntimeConfig.cachingStrategy = CachingStrategy.BASELINE_WITH_LOCK_SELECTABLE;
            verifyParentAndChild();

            // given the treatment strategy, the same delegation holds
            MotifRuntimeConfig.cachingStrategy = CachingStrategy.SMART_CACHE;
            verifyParentAndChild();
        } finally {
            MotifRuntimeConfig.cachingStrategy = original;
        }
    }

    private static void verifyParentAndChild() {
        // when a fresh scope is created and a child is requested
        Scope scope = new ScopeImpl();

        // then parent factory methods resolve through the delegate
        assertThat(scope.string()).isEqualTo("p");

        // and the child scope is reachable and functional
        Child child = scope.child();
        assertThat(child.string()).isEqualTo("c");

        // and each child() call returns a new child scope
        assertThat(scope.child()).isNotSameInstanceAs(child);
    }
}
