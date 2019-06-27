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
package testcases.T063_diamond_expose_to_grandchild;

import motif.Expose;

@motif.Scope
public interface Scope {

    ChildA childA();

    ChildB childB();

    @motif.Objects
    class Objects {

        @Expose
        String string() {
            return "s";
        }
    }

    @motif.Dependencies
    interface Dependencies {}
}
