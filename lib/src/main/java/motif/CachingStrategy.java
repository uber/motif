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
 * Defines the caching strategy used by Motif for dependency instantiation.
 *
 * <p>The caching strategy determines how Motif generates code for cached dependencies, affecting
 * both memory usage and thread synchronization patterns.
 */
public enum CachingStrategy {
  /**
   * BASELINE strategy (default): - Uses None.NONE sentinel pattern for uninitialized cache fields -
   * Uses synchronized(this) for thread-safe lazy initialization - Matches the traditional Motif
   * code generation behavior (alpha09 and earlier) - Cache fields are volatile Object initialized
   * to None.NONE - Double-checked locking pattern with synchronized(this)
   *
   * <p>This is the proven, stable strategy that maintains backward compatibility.
   */
  BASELINE,

  /**
   * BASELINE_WITH_LOCK_SELECTABLE strategy: - Like BASELINE but supports optional per-dependency
   * locks - When MotifRuntimeConfig.usePerDependencyLock is true, uses MotifLock instances - When
   * MotifRuntimeConfig.usePerDependencyLock is false, falls back to synchronized(this) - Lock
   * fields are nullable and conditionally initialized at construction time - Enables runtime
   * selection of locking granularity
   *
   * <p>This strategy allows applications to choose between coarse-grained (synchronized(this)) and
   * fine-grained (per-dependency locks) synchronization at runtime.
   */
  BASELINE_WITH_LOCK_SELECTABLE,

  /**
   * SMART_CACHE strategy: - Uses null for uninitialized cache fields (instead of None.NONE
   * sentinel) - Cache fields are volatile nullable types initialized to null - Supports optional
   * per-dependency locks via MotifRuntimeConfig.usePerDependencyLock - When per-dependency locks
   * enabled, uses MotifLock instances - When per-dependency locks disabled, falls back to
   * synchronized(this) - Double-checked locking with null check instead of None.NONE check -
   * Reduces memory footprint by eliminating None.NONE sentinel objects
   *
   * <p>This strategy is semantically equivalent to BASELINE but more memory-efficient. It uses the
   * JVM's native null representation instead of sentinel objects.
   */
  SMART_CACHE,

  /**
   * RUNTIME_SELECTABLE strategy: - Generates both BASELINE_WITH_LOCK_SELECTABLE and SMART_CACHE
   * implementations as variant classes - Creates a wrapper class that delegates to the selected
   * implementation - Runtime selection via MotifRuntimeConfig.cachingStrategy - Allows switching
   * between strategies without recompilation - At runtime, wrapper checks
   * MotifRuntimeConfig.cachingStrategy and instantiates appropriate variant - Variant classes are
   * named: ScopeImpl_BaselineSelectableLock and ScopeImpl_SmartCache - Wrapper class maintains
   * original ScopeImpl name for compatibility
   *
   * <p>This strategy provides maximum flexibility by generating both implementations and allowing
   * the application to choose at runtime which caching strategy to use. The wrapper class has
   * minimal overhead - just a single delegation call.
   *
   * <p>Use this strategy when you want to: - Test different caching strategies without
   * recompilation - Choose caching strategy based on device characteristics or runtime conditions -
   * Provide configurable caching behavior to end users
   */
  RUNTIME_SELECTABLE
}
