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
package testcases.T088_smart_cache_spread_source;

import motif.Creatable;
import motif.Spread;

// Regression test: a @Spread source must be cached under SMART_CACHE.
//
// Each spread method compiles to `return spreadable().facetX()`, so the accessors are
// independent call paths back into the source provider. countInternalUsage() only counts
// factory method parameters, so it sees zero consumers here and the unused rule used to
// leave the source uncached - making every facet come from a different Spreadable.
@motif.Scope(cachingStrategy = motif.CachingStrategy.SMART_CACHE)
public interface Scope extends Creatable<Scope.Dependencies> {

    FacetA facetA();

    FacetB facetB();

    @motif.Objects
    class Objects {

        // Regression target: spread source with no factory method consumer. Must be cached.
        @Spread
        Spreadable spreadable() {
            return new Spreadable();
        }

        // Control: not exposed, not spread and unused. Genuinely unused, must stay uncached.
        PlainUnusedDep plainUnusedDep() {
            return new PlainUnusedDep();
        }
    }

    interface Dependencies {}
}
