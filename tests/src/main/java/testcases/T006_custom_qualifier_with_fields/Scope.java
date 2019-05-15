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
package testcases.T006_custom_qualifier_with_fields;

@motif.Scope
public interface Scope {

    String string();

    @motif.Objects
    class Objects {

        String s(
                @CustomQualifier(first = "1", second = "2") String a,
                @CustomQualifier(first = "3", second = "4") String b) {
            return a + b;
        }

        @CustomQualifier(first = "1", second = "2")
        String a() {
            return "a";
        }

        @CustomQualifier(first = "3", second = "4")
        String b() {
            return "b";
        }
    }

    @motif.Dependencies
    interface Dependencies {}
}
