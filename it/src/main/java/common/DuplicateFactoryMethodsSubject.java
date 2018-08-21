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
import motif.compiler.javax.Executable;
import motif.ir.graph.DuplicateFactoryMethod;
import motif.ir.graph.errors.DuplicateFactoryMethodsError;
import motif.ir.source.base.Type;
import motif.ir.source.objects.FactoryMethod;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertAbout;

public class DuplicateFactoryMethodsSubject extends Subject<DuplicateFactoryMethodsSubject, DuplicateFactoryMethodsError> {

    private static final Factory<DuplicateFactoryMethodsSubject, DuplicateFactoryMethodsError> FACTORY = DuplicateFactoryMethodsSubject::new;

    private DuplicateFactoryMethodsSubject(FailureMetadata metadata, @NullableDecl DuplicateFactoryMethodsError actual) {
        super(metadata, actual);
    }

    public void matches(String expectedDuplicateName, Class<?>... expectedExistingClasses) {
        assertThat(actual()).isNotNull();

        String actualDuplicateName = getName(actual().getDuplicate());
        if (!actualDuplicateName.equals(expectedDuplicateName)) {
            failWithoutActual(
                    Fact.fact("expected", expectedDuplicateName),
                    Fact.fact("but was", actualDuplicateName));
        }

        Set<Type> expectedExisting = Arrays.stream(expectedExistingClasses)
                .map(aClass -> new Type(null, aClass.getName()))
                .collect(Collectors.toSet());

        if (!actual().getExisting().equals(expectedExisting)) {
            failWithoutActual(
                    Fact.fact("expected", expectedExisting),
                    Fact.fact("but was", actual().getExisting()));
        }
    }

    private static String getName(FactoryMethod factoryMethod) {
        return ((Executable) factoryMethod.getUserData()).getName();
    }

    public static DuplicateFactoryMethodsSubject assertThat(DuplicateFactoryMethodsError dependencyCycle) {
        return assertAbout(FACTORY).that(dependencyCycle);
    }
}
