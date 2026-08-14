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
package testcases.T089_smart_cache_expose_passthrough;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class Test {

    public static void run() {
        // given a SMART_CACHE scope whose exposed dependency is an abstract passthrough
        Scope scope = new ScopeImpl();

        // then the passthrough is cached despite the Binds shape
        verifyDeclareFieldVolatile("foo");

        // when two child scopes each request the exposed dependency
        Foo fromChild = scope.child().foo();
        Foo fromOtherChild = scope.otherChild().foo();

        // then both received the same instance
        assertThat(fromChild).isSameInstanceAs(fromOtherChild);

        // and a repeat call through the same child also returns that instance
        assertThat(fromChild).isSameInstanceAs(scope.child().foo());
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
}
