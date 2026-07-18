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
package testcases.T081_smart_cache_do_not_cache_wrapper;

import motif.Creatable;
import motif.DoNotCache;
import testcases.T081_smart_cache_do_not_cache_wrapper.dependency.DoNotCacheDep;
import testcases.T081_smart_cache_do_not_cache_wrapper.dependency.ExposedDep;
import testcases.T081_smart_cache_do_not_cache_wrapper.dependency.NotExposedDep;

@motif.Scope(cachingStrategy = motif.CachingStrategy.SMART_CACHE)
public interface Scope extends Creatable<Scope.Dependencies> {

    // Helper to verify caching strategy
    motif.CachingStrategy getCachingStrategy();

    DoNotCacheWrapper doNotCacheWrapper();

    @motif.Objects
    class Objects {

        @DoNotCache
        DoNotCacheWrapper exposedDepWrapper(ExposedDep exposedDep, NotExposedDep notExposedDep, DoNotCacheDep doNotCacheDep) {
            return new DoNotCacheWrapper(exposedDep, notExposedDep, doNotCacheDep);
        }

        // Dependency with @DoNotCache annotation
        @motif.DoNotCache
        DoNotCacheDep doNotCacheDep() {
            return new DoNotCacheDep();
        }

        // Dependency with @Expose annotation
        // Rule 7: @Expose annotation - should be cached
        @motif.Expose
        ExposedDep exposedDep() {
            return new ExposedDep();
        }

        NotExposedDep notExposedDep() {
            return new NotExposedDep();
        }

        motif.CachingStrategy getCachingStrategy() {
            return motif.CachingStrategy.SMART_CACHE;
        }
    }

    interface Dependencies {}
}
