/*
 * Copyright (c) 2018-2019 Uber Technologies, Inc.
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
package testcases.T078_use_null_field_concurrency_java;

import static com.google.common.truth.Truth.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Test {

    /**
     * This tests if the ScopeImpl synchronized blocks check and assign correct values
     */
    public static void run() {
        Scope scope = new ScopeImpl();
        int nThreads = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
        CountDownLatch getFooObjectLatch = new CountDownLatch(nThreads);
        CountDownLatch end = new CountDownLatch(nThreads);
        AtomicBoolean isFooObjectNull = new AtomicBoolean(false);
        try {
            synchronized (scope) { // blocks the synchronized in scope.fooObject
                for (int i = 0; i < nThreads; i++) {
                    executorService.submit(() -> {
                        getFooObjectLatch.countDown();
                        Object fooObject = scope.fooObject();
                        if(fooObject == null) {
                            isFooObjectNull.set(true);
                        }
                        end.countDown();
                    });
                }
                getFooObjectLatch.await(1000, TimeUnit.MILLISECONDS);
            }
            // at this point, the two threads will compete to create the fooObject

            // Verify
            if(end.await(1000, TimeUnit.MILLISECONDS)) {
                assertThat(isFooObjectNull.get()).isFalse();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
