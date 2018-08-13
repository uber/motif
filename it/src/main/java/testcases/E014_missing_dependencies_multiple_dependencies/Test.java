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
package testcases.E014_missing_dependencies_multiple_dependencies;

import com.google.common.truth.Truth;
import common.MissingDependenciesSubject;
import motif.ir.graph.errors.GraphValidationErrors;
import motif.ir.graph.errors.MissingDependenciesError;

import java.util.List;

public class Test {

    public static GraphValidationErrors errors;

    public static void run() {
        List<MissingDependenciesError> errors = Test.errors.getMissingDependenciesErrors();
        Truth.assertThat(errors).hasSize(1);
        MissingDependenciesError error = errors.get(0);
        MissingDependenciesSubject.assertThat(error)
                .matches(Scope.class, String.class, Integer.class);
    }
}
