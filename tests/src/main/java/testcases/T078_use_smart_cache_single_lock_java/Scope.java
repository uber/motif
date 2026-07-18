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
package testcases.T078_use_smart_cache_single_lock_java;

import java.util.concurrent.TimeUnit;
import motif.Creatable;

@motif.Scope(cachingStrategy = motif.CachingStrategy.SMART_CACHE)
public interface Scope extends Creatable<Scope.Dependencies> {

    SlowDep slowDep();
    FastDep fastDep();

    @motif.Objects
    class Objects {

        // Signals latches.started once inside the synchronized block, then blocks on
        // latches.release. This lets the test deterministically wait until slowDep holds
        // the lock before measuring fastDep.
        SlowDep slowDep(Latches latches) {
            latches.started.countDown();
            try {
                latches.release.await(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return new SlowDep();
        }

        FastDep fastDep() {
            return new FastDep();
        }

    }

    interface Dependencies {
        Latches latches();
    }
}
