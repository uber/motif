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
package testcases.T087_smart_cache_expose_no_internal_usage;

import motif.Creatable;
import testcases.T087_smart_cache_expose_no_internal_usage.dependency.ExposedAndUsedDep;
import testcases.T087_smart_cache_expose_no_internal_usage.dependency.PlainUnusedDep;
import testcases.T087_smart_cache_expose_no_internal_usage.dependency.SharedStateDep;

// Regression test: @Expose must be honored even when internal usage count is zero.
// sharedStateDep() has no consumer in this scope's Objects class and no scope-interface
// accessor, so it used to be wrongly treated as unused and left uncached.
@motif.Scope(cachingStrategy = motif.CachingStrategy.SMART_CACHE)
public interface Scope extends Creatable<Scope.Dependencies> {

    @motif.Objects
    class Objects {

        // Regression target: @Expose with zero internal usage. Must be cached.
        @motif.Expose
        SharedStateDep sharedStateDep() {
            return new SharedStateDep();
        }

        // Control: @Expose with internal usage (consumed by consumer). Must stay cached.
        @motif.Expose
        ExposedAndUsedDep exposedAndUsedDep() {
            return new ExposedAndUsedDep();
        }

        // Control: not exposed and unused. Genuinely unused, must remain uncached.
        PlainUnusedDep plainUnusedDep() {
            return new PlainUnusedDep();
        }

        String consumer(ExposedAndUsedDep dep) {
            return "test";
        }
    }

    interface Dependencies {}
}
