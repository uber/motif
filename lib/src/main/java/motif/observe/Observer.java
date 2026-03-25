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

/**
 * Observer interface for tracking Scope lifecycle events.
 * Implementations can be registered via {@link MotifObserver#register(Observer)}.
 */
public interface Observer {

  /**
   * Called when a Scope implementation is being initialized.
   *
   * @param scopeClassName the fully qualified name of the scope being initialized
   */
  void onScopeInitializing(String scopeClassName);

  /**
   * Called when a factory/provider method is about to be invoked.
   *
   * @param scopeClassName the fully qualified name of the scope
   * @param methodName the name of the factory/provider method
   */
  void onProvideStart(String scopeClassName, String methodName);

  /**
   * Called when a factory/provider method has completed execution.
   *
   * @param scopeClassName the fully qualified name of the scope
   * @param methodName the name of the factory/provider method
   */
  void onProvideComplete(String scopeClassName, String methodName);
}
