/*
 * Copyright (c) 2026 Uber Technologies, Inc.
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
import androidx.room.compiler.processing.util.compileFiles
import androidx.room.compiler.processing.util.runKspTest
import com.google.common.truth.Truth.assertThat
import java.io.File
import org.junit.Test

@OptIn(ExperimentalProcessingApi::class)
class CompilerTypeTest {

  @Test
  fun assigningChildToNonGenericInterfaceDoesNotInspectSupertypes() {
    val externalClasspath =
        compileFiles(
            sources =
                listOf(
                    Source.java("test.Parent", "package test; public class Parent {}"),
                    Source.java(
                        "test.Capabilities",
                        """
                        package test;
                        public interface Capabilities {
                          interface Foo {}
                          interface Bar {}
                        }
                        """
                            .trimIndent(),
                    ),
                ),
        )
    val localClasspath =
        compileFiles(
            sources =
                listOf(
                    Source.java("test.Target", "package test; public interface Target {}"),
                    Source.java(
                        "test.Child",
                        """
                        package test;
                        public class Child extends Parent
                            implements Target, Capabilities.Foo, Capabilities.Bar {}
                        """
                            .trimIndent(),
                    ),
                ),
            javacArguments =
                listOf("-classpath", externalClasspath.joinToString(File.pathSeparator)),
        )

    // Match a direct dependency whose own supertypes are absent from the consumer's strict
    // classpath.
    runKspTest(
        sources = listOf(Source.kotlin("test/Trigger.kt", "package test\nclass Trigger")),
        classpath = localClasspath,
    ) { invocation ->
      val env = invocation.processingEnv
      val child = CompilerType(env, env.requireType("test.Child"))
      val target = CompilerType(env, env.requireType("test.Target"))

      assertThat(child.isAssignableTo(target)).isTrue()
      invocation.assertCompilationResult { hasErrorCount(0) }
    }
  }
}
