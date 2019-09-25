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
package testcases.E048_duplicate_factory_method_spread2;

import motif.Creatable;
import motif.Expose;
import motif.Spread;

@motif.Scope
public interface Scope extends Creatable<Scope.Dependencies> {

    Child child();

    @motif.Objects
    class Objects {

        @Expose
        @Spread
        Foo foo() {
            return new Foo();
        }
    }

    interface Dependencies {}
}
