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
package testcases.T085_runtime_selectable_child;

import motif.Creatable;
import motif.CachingStrategy;

// RUNTIME_SELECTABLE parent with a child scope. Exercises the wrapper's child-method
// delegation under both the control and treatment variants.
@motif.Scope(cachingStrategy = CachingStrategy.RUNTIME_SELECTABLE)
public interface Scope extends Creatable<Scope.Dependencies> {

    String string();

    Child child();

    @motif.Objects
    class Objects {

        String string() {
            return "p";
        }
    }

    interface Dependencies {}
}
