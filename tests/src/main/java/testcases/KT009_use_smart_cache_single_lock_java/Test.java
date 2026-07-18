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
package testcases.KT009_use_smart_cache_single_lock_java;

import static com.google.common.truth.Truth.assertThat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import motif.MotifRuntimeConfig;

public class Test {

    /**
     * With usePerDependencyLock = false, all dependencies share synchronized(this),
     * so a fastDep() call must block while slowDep() holds the lock.
     */
    public static void run() throws Exception {
        // given a scope where slowDep signals once it holds the lock, then blocks
        MotifRuntimeConfig.usePerDependencyLock = false;

        Latches latches = new Latches();
        Scope scope = new ScopeImpl(() -> latches);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            // when slowDep runs and is confirmed to hold the lock
            Future<SlowDep> slowFuture = executor.submit(scope::slowDep);
            assertThat(latches.getStarted().await(1, TimeUnit.SECONDS)).isTrue();

            // then fastDep blocks because it needs the same lock
            Future<FastDep> fastFuture = executor.submit(scope::fastDep);
            TimeoutException exception = null;
            try {
                fastFuture.get(100, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                exception = e;
            }
            assertThat(exception).isNotNull();

            // and once the lock is released both calls complete
            latches.getRelease().countDown();
            slowFuture.get(1, TimeUnit.SECONDS);
            fastFuture.get(1, TimeUnit.SECONDS);
        } finally {
            executor.shutdown();
        }
    }
}
