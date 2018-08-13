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
package testcases.T009_dependency_cache;

import javax.inject.Named;

@motif.Scope
public interface Scope {

    String string();

    @motif.Objects
    class Objects {

        int i = 0;

        String string(@Named("a") String a, @Named("i") String i) {
            return a + i;
        }

        @Named("a")
        String a(@Named("i") String i1, @Named("i") String i2) {
            return "a" + i1 + i2;
        }

        @Named("i")
        String i() {
            return String.valueOf(i++);
        }
    }

    @motif.Dependencies
    interface Dependencies {}
}
