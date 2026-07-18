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
package testcases.T079_use_smart_cache_multi_lock_java;

import static com.google.common.truth.Truth.assertThat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import motif.MotifRuntimeConfig;

public class Test {

    /**
     * With usePerDependencyLock = true, each dependency uses its own lock, so fastDep()
     * can initialize while slowDep1() holds a different lock.
     */
    public static void run() throws Exception {
        // given a scope where slowDep1 signals once it holds its lock, then blocks
        MotifRuntimeConfig.usePerDependencyLock = true;

        Latches latches = new Latches();
        Scope scope = new ScopeImpl(() -> latches);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            // when slowDep1 runs and is confirmed to hold its lock
            Future<SlowDep1> slowFuture = executor.submit(scope::slowDep1);
            assertThat(latches.started.await(1, TimeUnit.SECONDS)).isTrue();

            // then fastDep still completes because it uses a different lock
            FastDep fastDep = executor.submit(scope::fastDep).get(1, TimeUnit.SECONDS);
            assertThat(fastDep).isNotNull();

            // cleanup: release slowDep1
            latches.release.countDown();
            slowFuture.get(1, TimeUnit.SECONDS);
        } finally {
            executor.shutdown();
        }
    }
}
