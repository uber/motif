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
package common;

import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import com.google.common.truth.Truth;
import motif.models.graph.errors.MissingDependenciesError;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertAbout;

public class MissingDependenciesSubject extends Subject<MissingDependenciesSubject, MissingDependenciesError> {

    private static final Subject.Factory<MissingDependenciesSubject, MissingDependenciesError> FACTORY = MissingDependenciesSubject::new;

    private MissingDependenciesSubject(FailureMetadata metadata, @NullableDecl MissingDependenciesError actual) {
        super(metadata, actual);
    }

    public void matches(Class<?> scopeClass, Class<?>... dependencies) {
        assertThat(actual()).isNotNull();

        String expectedScopeName = scopeClass.getName();
        String actualScopeName = actual().getRequiredBy().getScopeClass().getIr().getType().getQualifiedName();
        Truth.assertThat(actualScopeName).isEqualTo(expectedScopeName);

        List<String> expectedDependencies = Arrays.stream(dependencies)
                .map(Class::getName)
                .collect(Collectors.toList());

        List<String> actualDependencies = actual().getDependencies().stream()
                .map(dependency -> dependency.getType().getQualifiedName())
                .collect(Collectors.toList());

        Truth.assertThat(actualDependencies).containsExactlyElementsIn(expectedDependencies);
    }

    public static MissingDependenciesSubject assertThat(MissingDependenciesError error) {
        return assertAbout(FACTORY).that(error);
    }
}
