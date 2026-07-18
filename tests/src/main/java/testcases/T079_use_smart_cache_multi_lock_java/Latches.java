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

import java.util.concurrent.CountDownLatch;

// Wraps two CountDownLatches into one type to avoid [DUPLICATED DEPENDENCIES METHOD]
// since Motif requires distinct types in Dependencies.
public class Latches {
    public final CountDownLatch started = new CountDownLatch(1);
    public final CountDownLatch release = new CountDownLatch(1);
}
