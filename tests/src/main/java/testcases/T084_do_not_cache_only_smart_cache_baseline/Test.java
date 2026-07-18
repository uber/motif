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
package testcases.T084_do_not_cache_only_smart_cache_baseline;

import static com.google.common.truth.Truth.assertThat;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class Test {

    public static void run() throws NoSuchFieldException {
        // given a BASELINE scope with @DoNotCache(onlyForSmartCacheMode = true)
        Scope scope = new ScopeImpl();

        // when the dependency is requested twice
        Object first = scope.onlySmartCacheDep();
        Object second = scope.onlySmartCacheDep();

        // then it is cached (onlyForSmartCacheMode only disables caching in SMART_CACHE)
        assertThat(first).isSameInstanceAs(second);
        verifyDeclareFieldVolatile("onlySmartCacheDep");
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
}
