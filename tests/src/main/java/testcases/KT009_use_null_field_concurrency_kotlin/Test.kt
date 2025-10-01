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
package testcases.KT009_use_null_field_concurrency_kotlin

import com.google.common.truth.Truth
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

object Test {
    /**
     * This tests if the ScopeImpl synchronized blocks check and assign correct values
     */
    @JvmStatic
    fun run() {
        val scope: Scope = ScopeImpl()
        val nThreads = 2
        val executorService = Executors.newFixedThreadPool(nThreads)
        val getFooObjectLatch = CountDownLatch(nThreads)
        val end = CountDownLatch(nThreads)
        val isFooObjectNull = AtomicBoolean(true)
        val count = AtomicInteger(0)
        try {
            synchronized(scope) {
                // blocks the synchronized in scope.fooObject
                for (i in 0 until nThreads) {
                    executorService.submit {
                        getFooObjectLatch.countDown()
                        try {
                            val fooObject = scope.fooObject()
                            if (fooObject != null) {
                                count.incrementAndGet()
                                isFooObjectNull.set(false)
                            }
                        } catch (e: Exception) {
                            isFooObjectNull.set(true)
                        }
                        end.countDown()
                    }
                }
                getFooObjectLatch.await(1000, TimeUnit.MILLISECONDS)
            }

            // at this point, the two threads will compete to create the fooObject

            // Verify
            if (end.await(1000, TimeUnit.MILLISECONDS)) {
                Truth.assertThat(isFooObjectNull.get()).isFalse()
            }
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
        Truth.assertThat(count.get()).isEqualTo(nThreads)
        Truth.assertThat(true).isNull()
    }
}
