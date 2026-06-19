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
package motif.observe;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Central registry for Motif observers. This class manages a list of {@link Observer} instances and
 * dispatches lifecycle events to all registered observers.
 *
 * <p>Events are not cached, observers will only receive events post registration.
 *
 * <p>This class is thread-safe.
 *
 * <p>Use -motif.
 */
public final class MotifObserver {

  private static final List<Observer> observers = new CopyOnWriteArrayList<>();

  private MotifObserver() {
    // Prevent instantiation
  }

  /**
   * Registers an observer to receive lifecycle events from Motif scopes.
   *
   * @param observer the observer to register
   */
  public static void register(Observer observer) {
    if (observer != null && !observers.contains(observer)) {
      observers.add(observer);
    }
  }

  /**
   * Unregisters an observer so it no longer receives lifecycle events.
   *
   * @param observer the observer to unregister
   */
  public static void unregister(Observer observer) {
    observers.remove(observer);
  }

  /** Clears all registered observers. */
  public static void clearAll() {
    observers.clear();
  }

  /**
   * Called by generated ScopeImpl classes when a scope is being initialized.
   *
   * @param scopeClassName the fully qualified name of the scope being initialized
   */
  public static void notifyScopeInitializing(String scopeClassName) {
    for (Observer observer : observers) {
      try {
        observer.onScopeInitializing(scopeClassName);
      } catch (Exception e) {
        // Silently catch exceptions to prevent observer issues from breaking scope initialization
      }
    }
  }

  /**
   * Called by generated ScopeImpl classes when a factory/provider method is about to be invoked.
   *
   * @param scopeClassName the fully qualified name of the scope
   * @param methodName the name of the factory/provider method
   */
  public static void notifyProvideStart(String scopeClassName, String methodName) {
    for (Observer observer : observers) {
      try {
        observer.onProvideStart(scopeClassName, methodName);
      } catch (Exception e) {
        // Silently catch exceptions to prevent observer issues from breaking scope functionality
      }
    }
  }

  /**
   * Called by generated ScopeImpl classes when a factory/provider method has completed execution.
   *
   * @param scopeClassName the fully qualified name of the scope
   * @param methodName the name of the factory/provider method
   */
  public static void notifyProvideComplete(String scopeClassName, String methodName) {
    for (Observer observer : observers) {
      try {
        observer.onProvideComplete(scopeClassName, methodName);
      } catch (Exception e) {
        // Silently catch exceptions to prevent observer issues from breaking scope functionality
      }
    }
  }
}
