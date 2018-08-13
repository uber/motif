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
import motif.ir.source.objects.FactoryMethod;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertAbout;

public class DuplicateFactoryMethodsSubject extends Subject<DuplicateFactoryMethodsSubject, DuplicateFactoryMethodsError> {

    private static final Factory<DuplicateFactoryMethodsSubject, DuplicateFactoryMethodsError> FACTORY = DuplicateFactoryMethodsSubject::new;

    private final Map<String, List<String>> expectedDuplicates = new HashMap<>();

    private DuplicateFactoryMethodsSubject(FailureMetadata metadata, @NullableDecl DuplicateFactoryMethodsError actual) {
        super(metadata, actual);
    }

    public void matches() {
        assertThat(actual()).isNotNull();

        Map<String, List<String>> actualDuplicates = new HashMap<>();
        for (DuplicateFactoryMethod duplicate : actual().getDuplicates()) {
            String duplicateName = getName(duplicate.getDuplicate());
            List<String> existingNames = duplicate.getExisting().stream().map(DuplicateFactoryMethodsSubject::getName).collect(Collectors.toList());
            actualDuplicates.put(duplicateName, existingNames);
        }

        if (!actualDuplicates.equals(expectedDuplicates)) {
            failWithoutActual(
                    Fact.fact("expected", expectedDuplicates),
                    Fact.fact("but was", actualDuplicates));
        }
    }

    private static String getName(FactoryMethod factoryMethod) {
        return ((Executable) factoryMethod.getUserData()).getName();
    }

    public DuplicateFactoryMethodsSubject with(String duplicateFactoryMethodName, String... existingFactoryMethodNames) {
        expectedDuplicates.put(duplicateFactoryMethodName, Arrays.asList(existingFactoryMethodNames));
        return this;
    }

    public static DuplicateFactoryMethodsSubject assertThat(DuplicateFactoryMethodsError dependencyCycle) {
        return assertAbout(FACTORY).that(dependencyCycle);
    }
}
