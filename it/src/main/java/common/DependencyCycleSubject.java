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

import com.google.common.truth.Fact;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import motif.ir.graph.DependencyCycle;
import motif.ir.graph.errors.DependencyCycleError;
import motif.ir.source.base.Annotation;
import motif.ir.source.base.Dependency;
import motif.ir.source.base.Type;
import motif.ir.source.objects.FactoryMethod;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertAbout;

public class DependencyCycleSubject extends Subject<DependencyCycleSubject, DependencyCycleError> {

    private static final Factory<DependencyCycleSubject, DependencyCycleError> FACTORY = DependencyCycleSubject::new;

    private DependencyCycleSubject(FailureMetadata metadata, @NullableDecl DependencyCycleError actual) {
        super(metadata, actual);
    }

    public void matches(Class<?> scopeClass, String... cycleNames) {
        assertThat(actual()).isNotNull();

        List<Dependency> expectedCycle = Arrays.stream(cycleNames)
                .map(cycleName -> new Dependency(
                        null,
                        new Type(null, "java.lang.String"),
                        new Annotation(null, "@javax.inject.Named(\"" + cycleName + "\")")))
                .collect(Collectors.toList());

        List<Dependency> actualCycle = actual().getCycle().stream()
                .map(FactoryMethod::getProvidedDependency)
                .collect(Collectors.toList());

        Type expectedScopeType = new Type(null, scopeClass.getName());
        Type actualScopeType = actual().getScopeClass().getType();
        if (!expectedScopeType.equals(actualScopeType)) {
            failWithoutActual(
                    Fact.fact("expected", expectedScopeType),
                    Fact.fact("but was", actualScopeType));
        }

        if (!actualCycle.equals(expectedCycle)) {
            failWithoutActual(
                    Fact.fact("expected", expectedCycle),
                    Fact.fact("but was", actualCycle));
        }
    }

    public static DependencyCycleSubject assertThat(DependencyCycleError dependencyCycle) {
        return assertAbout(FACTORY).that(dependencyCycle);
    }
}
