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
import motif.models.errors.DependencyCycleError;
import motif.models.motif.dependencies.Dependency;
import motif.models.motif.objects.FactoryMethod;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertAbout;

public class DependencyCycleSubject extends Subject<DependencyCycleSubject, DependencyCycleError> {

    private static final Factory<DependencyCycleSubject, DependencyCycleError> FACTORY = DependencyCycleSubject::new;

    private DependencyCycleSubject(FailureMetadata metadata, @NullableDecl DependencyCycleError actual) {
        super(metadata, actual);
    }

    public void matches(Class<?> scopeClass, String... cycleNames) {
        assertThat(actual()).isNotNull();

        String expectedScopeName = scopeClass.getName();
        String actualScopeName = actual().getScopeClass().getIr().getType().getQualifiedName();
        Truth.assertThat(actualScopeName).isEqualTo(expectedScopeName);

        List<String> expectedCycle = Arrays.stream(cycleNames)
                .map(s -> "@javax.inject.Named(\"" + s + "\") java.lang.String")
                .collect(Collectors.toList());

        List<String> actualCycle = actual().getCycle().stream()
                .map(FactoryMethod::getProvidedDependency)
                .map(Dependency::toString)
                .collect(Collectors.toList());

        Truth.assertThat(actualCycle).isEqualTo(expectedCycle);
    }

    public static DependencyCycleSubject assertThat(DependencyCycleError dependencyCycle) {
        return assertAbout(FACTORY).that(dependencyCycle);
    }
}
