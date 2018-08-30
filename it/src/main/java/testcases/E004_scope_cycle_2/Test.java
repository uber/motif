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
package testcases.E004_scope_cycle_2;

import com.google.common.truth.Truth;
import motif.models.graph.errors.GraphValidationErrors;
import motif.models.graph.errors.ScopeCycleError;
import motif.models.java.IrType;

import java.util.stream.Collectors;

public class Test {

    public static GraphValidationErrors errors;

    public static void run() {
        ScopeCycleError error = errors.getScopeCycleError();
        Truth.assertThat(error).isNotNull();
        Truth.assertThat(error.getCycle().stream().map(IrType::getQualifiedName).collect(Collectors.toList()))
                .containsExactly(
                        Scope.class.getName(),
                        Child.class.getName());
    }
}
