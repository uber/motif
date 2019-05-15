/*
 * Copyright (c) 2018-2019 Uber Technologies, Inc.
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
package motif.ast

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class SimpleNameTest(private val qualifiedName: String, private val expectedSimpleName: String) {

    companion object {

        private val tests = mapOf(
                "java.lang.String" to "String",
                "java.util.List<java.lang.String>" to "List<String>",
                "java.util.List<? extends java.lang.String>" to "List<? extends String>",
                "java.util.List<? super java.lang.String>" to "List<? super String>",
                "java.util.Map<java.lang.String, java.lang.Integer>" to "Map<String, Integer>",
                "java.util.Map<? extends java.lang.String, ? super java.lang.Integer>" to "Map<? extends String, ? super Integer>")

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data() : Collection<Array<Any>> {
            return tests.map { (key, value) -> arrayOf<Any>(key, value) }
        }
    }

    @Test
    fun simpleNames() {
        val actualSimpleName = simpleName(qualifiedName)
        assertThat(actualSimpleName).isEqualTo(expectedSimpleName)
    }
}
