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
package testcases.T081_smart_cache_do_not_cache_wrapper;

import static com.google.common.truth.Truth.assertThat;


import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import motif.CachingStrategy;

public class Test {

    public static void run() throws NoSuchFieldException {
        Scope scope = new ScopeImpl();

        // Verify caching strategy is SMART_CACHE
        assertThat(scope.getCachingStrategy()).isEqualTo(CachingStrategy.SMART_CACHE);

        // Test Rule 7: Dependency with @Expose (should be cached)
        DoNotCacheWrapper wrapper1 = scope.doNotCacheWrapper();
        DoNotCacheWrapper wrapper2 = scope.doNotCacheWrapper();
        // Case DoNotExpose: No cache
        assertThat(wrapper1)
                .isNotSameInstanceAs(wrapper2);
        verifyDeclareFieldDoesNotExist("doNotCacheWrapper");

        // Case Exposed dependency
        assertThat(wrapper1.exposedDep)
                .isSameInstanceAs(wrapper2.exposedDep);
        verifyDeclareFieldVolatile("exposedDep");

        // Case Not Exposed dependency
        assertThat(wrapper1.notExposedDep)
                .isSameInstanceAs(wrapper2.notExposedDep);
        verifyDeclareFieldVolatile("notExposedDep");


        // Case @DoNotCache dependency
        assertThat(wrapper1.doNotCacheDep)
                .isNotSameInstanceAs(wrapper2.doNotCacheDep);
        verifyDeclareFieldDoesNotExist("typeDoNotCacheDep");

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
