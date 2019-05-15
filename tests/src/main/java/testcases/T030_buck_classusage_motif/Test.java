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
package testcases.T030_buck_classusage_motif;

import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

public class Test {

    public static Set<String> loadedClasses;

    public static void run() {
        // This is not ideal, but is a trade-off that we're documenting via this integration test.
        assertThat(loadedClasses).contains("external.T030_buck_classusage_motif.A");
    }
}
