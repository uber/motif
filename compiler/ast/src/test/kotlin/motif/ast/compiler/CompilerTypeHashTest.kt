/*
 * Copyright (c) 2024 Uber Technologies, Inc.
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
package motif.ast.compiler

import androidx.room.compiler.processing.ExperimentalProcessingApi
import androidx.room.compiler.processing.util.Source
import androidx.room.compiler.processing.util.runKspTest
import com.google.common.truth.Truth.assertThat
import com.uber.xprocessing.ext.hash
import com.uber.xprocessing.ext.isEquivalent
import org.junit.Test

@OptIn(ExperimentalProcessingApi::class)
class CompilerTypeHashTest {

  @Test
  fun hashEqualsContractForParameterizedType() {
    val sources =
        listOf(
            Source.kotlin(
                "test/Wrapper.kt",
                """
                package test
                class Wrapper<T>(val value: T)
                """
                    .trimIndent(),
            ),
            Source.java(
                "test.JavaUser",
                """
                package test;
                public class JavaUser {
                    public Wrapper<String> provide() { return null; }
                }
                """
                    .trimIndent(),
            ),
            Source.kotlin(
                "test/KotlinUser.kt",
                """
                package test
                class KotlinUser {
                    fun provide(): Wrapper<String> = Wrapper("s")
                }
                """
                    .trimIndent(),
            ),
        )

    runKspTest(sources) { invocation ->
      val env = invocation.processingEnv

      val javaClass = env.findTypeElement("test.JavaUser")!!
      val kotlinClass = env.findTypeElement("test.KotlinUser")!!

      val javaReturnType = javaClass.getDeclaredMethods().first { it.name == "provide" }.returnType
      val kotlinReturnType =
          kotlinClass.getDeclaredMethods().first { it.name == "provide" }.returnType

      assertThat(javaReturnType.isEquivalent(kotlinReturnType, env)).isTrue()
      assertThat(javaReturnType.hash()).isEqualTo(kotlinReturnType.hash())

      val type1 = CompilerType(env, javaReturnType)
      val type2 = CompilerType(env, kotlinReturnType)
      assertThat(type1).isEqualTo(type2)
      assertThat(type1.hashCode()).isEqualTo(type2.hashCode())

      val map = HashMap<CompilerType, String>()
      map[type1] = "first"
      map.putIfAbsent(type2, "should-not-appear")
      assertThat(map).hasSize(1)
      assertThat(map[type1]).isEqualTo("first")
      assertThat(map[type2]).isEqualTo("first")

      invocation.assertCompilationResult { hasErrorCount(0) }
    }
  }
}
