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
package testcases.KT010_use_smart_cache_multi_lock_java

import java.util.concurrent.TimeUnit
import motif.Creatable

@motif.Scope(cachingStrategy = motif.CachingStrategy.SMART_CACHE)
interface Scope : Creatable<Scope.Dependencies> {

    fun slowDep(): SlowDep
    fun fastDep(): FastDep

    @motif.Objects
    abstract class Objects {

        // Signals latches.started once holding its own lock, then blocks on
        // latches.release. The test waits for started before asserting fastDep can
        // proceed on a different lock.
        fun slowDep(latches: Latches): SlowDep {
            latches.started.countDown()
            try {
                latches.release.await(1, TimeUnit.SECONDS)
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            }
            return SlowDep()
        }

        fun fastDep(): FastDep {
            return FastDep()
        }

    }

    interface Dependencies {
        fun latches(): Latches
    }
}
