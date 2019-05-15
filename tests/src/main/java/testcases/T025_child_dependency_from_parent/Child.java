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
package testcases.T025_child_dependency_from_parent;

import motif.Scope;

import javax.inject.Named;

@Scope
public interface Child {

    @Named("c")
    String string();

    @motif.Objects
    class Objects {

        @Named("c")
        String string(@Named("p") String p) {
            return "c" + p;
        }
    }
}
