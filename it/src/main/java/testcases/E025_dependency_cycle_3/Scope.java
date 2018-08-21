/*
 * Copyright (c) 2018 Uber Technologies, Inc.
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
package testcases.E025_dependency_cycle_3;

import javax.inject.Named;

@motif.Scope
public interface Scope {

    @motif.Objects
    class Objects {

        @Named("a")
        String a(@Named("b") String b) {
            return "a";
        }

        @Named("b")
        String b(@Named("c") String c) {
            return "b";
        }

        @Named("c")
        String c(@Named("b") String b) {
            return "c";
        }
    }
}
