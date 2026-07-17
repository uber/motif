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
 * Runtime configuration for Motif code generation behavior.
 *
 * <p>These settings control how generated Motif code behaves at runtime, particularly
 * around caching strategies and synchronization patterns.
 *
 * <p><b>Thread Safety:</b> All fields are volatile to ensure visibility across threads.
 * Applications should set these values once during initialization, before any Motif
 * scopes are created.
 */
public final class MotifRuntimeConfig {

  /**
   * The caching strategy to use for dependency instantiation.
   *
   * <p>Default: {@link CachingStrategy#BASELINE}
   *
   * <p>This setting only affects scopes generated with RUNTIME_SELECTABLE strategy.
   * Scopes compiled with a specific strategy (BASELINE or BASELINE_WITH_LOCK_SELECTABLE)
   * always use their compiled strategy regardless of this setting.
   */
  public static volatile CachingStrategy cachingStrategy = CachingStrategy.BASELINE;

  /**
   * Whether to use per-dependency locks instead of synchronized(this).
   *
   * <p>Default: false (use synchronized(this))
   *
   * <p>When true, scopes compiled with BASELINE_WITH_LOCK_SELECTABLE will use
   * individual MotifLock instances for each cached dependency, enabling finer-grained
   * synchronization and better concurrency.
   *
   * <p>When false, all scopes fall back to synchronized(this) for simplicity and
   * lower memory overhead.
   *
   * <p><b>Performance Considerations:</b>
   * <ul>
   *   <li>true = Better concurrency, higher memory usage (one lock per dependency)</li>
   *   <li>false = Lower memory, coarser synchronization</li>
   * </ul>
   */
  public static volatile boolean usePerDependencyLock = false;

  private MotifRuntimeConfig() {
    // Static utility class
  }
}
