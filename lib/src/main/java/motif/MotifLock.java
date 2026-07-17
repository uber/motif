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
package motif;

/**
 * A lightweight lock object for per-dependency synchronization in Motif scopes.
 *
 * <p>This class exists primarily to make lock objects identifiable in heap dumps
 * and memory profilers, making it easier to analyze memory usage and contention.
 *
 * <p>Unlike using {@code Object} or {@code Any} as lock objects, MotifLock instances
 * have a distinct class name that appears in memory analysis tools, enabling developers
 * to quickly identify and measure the memory impact of per-dependency locking.
 *
 * <p><b>Memory Footprint:</b> This class is intentionally minimal with no fields
 * or methods, keeping its memory overhead as low as possible (just object header).
 *
 * <p><b>Usage:</b> Generated Motif code uses MotifLock instances when
 * {@link MotifRuntimeConfig#usePerDependencyLock} is enabled.
 */
public final class MotifLock {
    // Empty class - serves only as an identifiable lock object
}
