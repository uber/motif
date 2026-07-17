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
package testcases.T086_smart_cache_concurrent_contention;

import java.util.concurrent.atomic.AtomicInteger;
import motif.Creatable;

// Concurrency test scope: the cached factory increments a counter so the test can
// verify it runs exactly once even when many threads race to create it.
@motif.Scope(cachingStrategy = motif.CachingStrategy.SMART_CACHE)
public interface Scope extends Creatable<Scope.Dependencies> {

    Object cachedDep();

    @motif.Objects
    class Objects {

        Object cachedDep(AtomicInteger creationCount) {
            creationCount.incrementAndGet();
            return new Object();
        }
    }

    interface Dependencies {
        AtomicInteger creationCount();
    }
}
