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
package testcases.T080_smart_cache_selective_cache;

import motif.Creatable;
import testcases.T080_smart_cache_selective_cache.dependency.DoNotCacheDep;
import testcases.T080_smart_cache_selective_cache.dependency.DeadCodeDep;
import testcases.T080_smart_cache_selective_cache.dependency.ExposedDep;
import testcases.T080_smart_cache_selective_cache.dependency.MultiUseDep;
import testcases.T080_smart_cache_selective_cache.dependency.NotExposedDep;
import testcases.T080_smart_cache_selective_cache.dependency.PublicAccessorDep;
import testcases.T080_smart_cache_selective_cache.dependency.SingleUseDep;

@motif.Scope(cachingStrategy = motif.CachingStrategy.SMART_CACHE)
public interface Scope extends Creatable<Scope.Dependencies> {

    PublicAccessorDep publicAccessorDep();

    @motif.Objects
    class Objects {

        // Single-use internal dependency (only used by consumer1)
        SingleUseDep singleUseDep() {
            return new SingleUseDep();
        }

        // Multiple-use dependency (used by consumer1 and consumer2)
        MultiUseDep multiUseDep() {
            return new MultiUseDep();
        }

        String stringDep(MultiUseDep multiUseDep, ExposedDep exposedDep, SingleUseDep singleUseDep) {
            return "test";
        }

        Integer intDep(MultiUseDep multiUseDep) {
            return 1;
        }

        PublicAccessorDep publicAccessorDep() {
            return new PublicAccessorDep();
        }

        @motif.DoNotCache(onlyForSmartCacheMode = true)
        DoNotCacheDep doNotCacheDep() {
            return new DoNotCacheDep();
        }

        @motif.Expose
        DeadCodeDep deadCodeDep() {
            return new DeadCodeDep();
        }
        @motif.Expose
        ExposedDep exposedDep() {
            return new ExposedDep();
        }

        NotExposedDep notExposedDep() {
            return new NotExposedDep();
        }
    }

    interface Dependencies {}
}
