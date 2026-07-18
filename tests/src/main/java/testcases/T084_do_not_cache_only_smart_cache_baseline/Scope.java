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
package testcases.T084_do_not_cache_only_smart_cache_baseline;

import motif.Creatable;
import testcases.T084_do_not_cache_only_smart_cache_baseline.dependency.OnlySmartCacheDep;

// Verifies that @DoNotCache(onlyForSmartCacheMode = true) still caches under BASELINE.
// It only disables caching in SMART_CACHE mode.
@motif.Scope(cachingStrategy = motif.CachingStrategy.BASELINE)
public interface Scope extends Creatable<Scope.Dependencies> {

    OnlySmartCacheDep onlySmartCacheDep();

    @motif.Objects
    class Objects {

        @motif.DoNotCache(onlyForSmartCacheMode = true)
        OnlySmartCacheDep onlySmartCacheDep() {
            return new OnlySmartCacheDep();
        }
    }

    interface Dependencies {}
}
