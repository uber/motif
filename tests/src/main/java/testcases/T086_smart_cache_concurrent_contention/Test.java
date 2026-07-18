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

import static com.google.common.truth.Truth.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import motif.MotifRuntimeConfig;

public class Test {

    private static final int THREADS = 16;

    /**
     * Many threads race to read the same cached dependency. Double-checked locking must
     * ensure the factory runs exactly once and every caller sees the same non-null instance.
     * Runs under both single-lock and per-dependency-lock modes.
     */
    public static void run() throws Exception {
        boolean original = MotifRuntimeConfig.usePerDependencyLock;
        try {
            verifyCachedOnce(false);
            verifyCachedOnce(true);
        } finally {
            MotifRuntimeConfig.usePerDependencyLock = original;
        }
    }

    private static void verifyCachedOnce(boolean usePerDependencyLock) throws Exception {
        // given a fresh scope and many threads released simultaneously
        MotifRuntimeConfig.usePerDependencyLock = usePerDependencyLock;
        AtomicInteger creationCount = new AtomicInteger(0);
        Scope scope = new ScopeImpl(() -> creationCount);

        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        CyclicBarrier barrier = new CyclicBarrier(THREADS);
        try {
            List<Future<Object>> futures = new ArrayList<>();
            for (int i = 0; i < THREADS; i++) {
                futures.add(executor.submit((Callable<Object>) () -> {
                    barrier.await(); // maximize the race window
                    return scope.cachedDep();
                }));
            }

            // when all threads have returned
            Object first = futures.get(0).get(5, TimeUnit.SECONDS);

            // then every caller got the same non-null instance
            assertThat(first).isNotNull();
            for (Future<Object> future : futures) {
                assertThat(future.get(5, TimeUnit.SECONDS)).isSameInstanceAs(first);
            }

            // and the factory ran exactly once despite the contention
            assertThat(creationCount.get()).isEqualTo(1);
        } finally {
            executor.shutdown();
        }
    }
}
