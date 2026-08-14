/*
 * Copyright (c) 2025 Uber Technologies, Inc.
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
package testcases.T088_smart_cache_spread_source;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class Test {

    public static void run() {
        // given a SMART_CACHE scope whose only @Spread source has no factory method consumer
        Scope scope = new ScopeImpl();

        // then the source is cached
        verifyDeclareFieldVolatile("spreadable");

        // and a genuinely dead dependency is still not cached
        verifyDeclareFieldDoesNotExist("plainUnusedDep");

        // when both spread facets are requested
        FacetA facetA = scope.facetA();
        FacetB facetB = scope.facetB();

        // then they came from the same source instance
        assertThat(facetA.source).isSameInstanceAs(facetB.source);

        // and a repeat call to the same spread accessor also comes from that instance
        assertThat(scope.facetA().source).isSameInstanceAs(facetA.source);
    }

    private static void verifyDeclareFieldVolatile(String fieldName) {
        Field field;
        try {
            field = ScopeImpl.class.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            assertWithMessage("Field '" + fieldName + "' should be declared in ScopeImpl").fail();
            return;
        }
        assertThat(Modifier.isVolatile(field.getModifiers())).isTrue();
    }

    private static void verifyDeclareFieldDoesNotExist(String fieldName) {
        try {
            ScopeImpl.class.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            return;
        }
        assertWithMessage("Field '" + fieldName + "' should not be declared in ScopeImpl").fail();
    }
}
