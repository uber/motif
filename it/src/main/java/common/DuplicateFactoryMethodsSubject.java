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
import motif.models.errors.DuplicateFactoryMethodsError;
import motif.models.java.IrType;
import motif.models.motif.objects.FactoryMethod;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.Arrays;
import java.util.Set;
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
        Truth.assertThat(actualDuplicateName).isEqualTo(expectedDuplicateName);

        Set<String> expectedExisting = Arrays.stream(expectedExistingClasses)
                .map(Class::getName)
                .collect(Collectors.toSet());

        Set<String> actualExisting = actual().getExisting().stream()
                .map(IrType::getQualifiedName)
                .collect(Collectors.toSet());

        Truth.assertThat(actualExisting).isEqualTo(expectedExisting);
    }

    private static String getName(FactoryMethod factoryMethod) {
        return factoryMethod.getIr().getName();
    }

    public static DuplicateFactoryMethodsSubject assertThat(DuplicateFactoryMethodsError dependencyCycle) {
        return assertAbout(FACTORY).that(dependencyCycle);
    }
}
