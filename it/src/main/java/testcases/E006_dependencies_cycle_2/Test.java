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
package testcases.E006_dependencies_cycle_2;

import common.DependencyCycleSubject;
import motif.models.graph.errors.DependencyCycleError;
import motif.models.graph.errors.GraphValidationErrors;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class Test {

    public static GraphValidationErrors errors;

    public static void run() {
        List<DependencyCycleError> errors = Test.errors.getDependencyCycleErrors();
        assertThat(errors).hasSize(1);

        DependencyCycleError error = errors.get(0);
        DependencyCycleSubject.assertThat(error)
                .matches(Scope.class, "a", "b");
    }
}
