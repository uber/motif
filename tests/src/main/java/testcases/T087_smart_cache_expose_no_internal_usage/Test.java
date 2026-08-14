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
package testcases.T087_smart_cache_expose_no_internal_usage;

import static com.google.common.truth.Truth.assertThat;


import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class Test {

    public static void run() throws NoSuchFieldException {
        Scope scope = new ScopeImpl();

        // Regression: @Expose with zero internal usage count must still be cached
        // (usage-count unused rule does not apply to exposed deps).
        verifyDeclareFieldVolatile("sharedStateDep");

        // Control: @Expose with internal usage must remain cached.
        verifyDeclareFieldVolatile("exposedAndUsedDep");

        // Control: not exposed and unused is genuinely unused, must remain uncached.
        verifyDeclareFieldDoesNotExist("plainUnusedDep");
    }

    private static void verifyDeclareFieldVolatile(String fieldName) {
        Field field = null;
        try {
            field = ScopeImpl.class.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            assertThat("").containsMatch("Field '" + fieldName + "' not found in ScopeImpl");
        }
        boolean isVolatile = Modifier.isVolatile(field.getModifiers());
        assertThat(isVolatile).isTrue();
    }

    private static void verifyDeclareFieldDoesNotExist(String fieldName) {
        try {
            ScopeImpl.class.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            return;
        }
        assertThat("").contains("Field '" + fieldName + "' should not be declared in ScopeImpl");
    }
}
