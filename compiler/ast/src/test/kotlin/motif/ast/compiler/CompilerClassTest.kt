/*
 * Copyright (c) 2022 Uber Technologies, Inc.
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
import androidx.room.compiler.processing.util.XTestInvocation
import androidx.room.compiler.processing.util.runKspTest
import androidx.room.compiler.processing.util.runProcessorTestWithoutKsp
import com.google.common.collect.Sets.cartesianProduct
import com.google.common.truth.Truth.assertThat
import org.intellij.lang.annotations.Language
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
@OptIn(ExperimentalProcessingApi::class)
class XCompilerClassTest(
    private val processorType: ProcessorType,
    private val srcLang: SourceLanguage,
) {

  companion object {
    @JvmStatic
    @Parameterized.Parameters(name = "{0}_{1}")
    fun data(): Collection<Array<Any>> =
        cartesianProduct(
                ProcessorType.values().toSortedSet(),
                SourceLanguage.values().toSortedSet(),
            )
            .filterNot { (proc, _) -> proc == ProcessorType.KSP } // Investigate this after ksp#1086
            .filterNot { (proc, srcLang) ->
              proc == ProcessorType.AP && srcLang == SourceLanguage.KOTLIN
            }
            .map { it.toTypedArray() as Array<Any> }
            .toList()
  }

  @Test
  fun testSupertypeTypeArguments() {
    createClass(
        "test.Foo",
        """
          package test;

          abstract class Bar<T> {}

          class Foo extends Bar<String> {}
      """
            .trimIndent(),
    ) { fooClass ->
      val superClass = fooClass.supertypes.single().resolveClass() as CompilerClass
      assertThat(superClass.typeArguments.map { it.qualifiedName })
          .containsExactly("java.lang.String")
    }
  }

  @Test
  fun testSupertypeTypeArguments_intermediateClass() {
    createClass(
        "test.Foo",
        """
          package test;

          class Baz<T> {}

          class Bar<T> extends Baz<T> {}

          class Foo extends Bar<String> {}
      """
            .trimIndent(),
    ) { fooClass ->
      val barClass = fooClass.supertypes.single().resolveClass() as CompilerClass
      val bazClass = barClass.supertypes.single().resolveClass() as CompilerClass
      assertThat(bazClass.typeArguments.map { it.qualifiedName })
          .containsExactly("java.lang.String")
    }
  }

  @Test
  fun testSupertypeTypeArguments_typeVariable() {
    createClass(
        "test.Foo",
        """
          package test;

          class Bar<T> {}

          class Foo<T> extends Bar<T> {}
      """
            .trimIndent(),
    ) { fooClass ->
      val superClass = fooClass.supertypes.single().resolveClass() as CompilerClass
      assertThat(superClass.typeArguments).isEmpty()
    }
  }

  @Test
  fun testObjectSupertype() {
    createClass(
        "test.Foo",
        """
          package test;

          class Foo {}
      """
            .trimIndent(),
    ) { fooClass ->
      val objectClass = fooClass.supertypes.single().resolveClass()!!
      assertThat(objectClass.qualifiedName).isEqualTo(listOf("java.lang.Object").forLang().first())
      assertThat(objectClass.supertypes).isEmpty()
    }
  }

  @Test
  fun testSupertypeOnlyInterface() {
    createClass(
        "test.Foo",
        """
          package test;

          interface Bar {}

          class Foo implements Bar {}
      """
            .trimIndent(),
    ) { fooClass ->
      val superTypes = fooClass.supertypes.map { it.qualifiedName }
      assertThat(superTypes)
          .containsExactly(
              *listOf("java.lang.Object", "test.Bar")
                  .forLang()
                  .filter { it != "kotlin.Any" }
                  .toTypedArray(),
          )
    }
  }

  @Test
  fun testSupertypeMultipleInterfaces() {
    createClass(
        "test.Foo",
        """
          package test;

          interface Bar {}

          interface Baz {}

          class Foo implements Bar, Baz {}
      """
            .trimIndent(),
    ) { fooClass ->
      val superTypes = fooClass.supertypes.map { it.qualifiedName }
      assertThat(superTypes)
          .containsExactly(
              *listOf("java.lang.Object", "test.Bar", "test.Baz")
                  .forLang()
                  .filter { it != "kotlin.Any" }
                  .toTypedArray(),
          )
    }
  }

  private fun createClass(
      qualifiedName: String,
      @Language("JAVA") text: String,
      assertions: (CompilerClass) -> Unit,
  ) {
    val pkg =
        text
            .lines()
            .firstOrNull { it.startsWith("package ") }
            ?.substringAfter(" ")
            ?.substringBefore(";") ?: "test"
    val srcFiles = text.split("\n{2,}".toRegex()).filterNot { it.startsWith("package") }
    val sources =
        when (srcLang) {
          SourceLanguage.JAVA -> createJavaSources(srcFiles, pkg)
          SourceLanguage.KOTLIN -> createKotlinSources(srcFiles, pkg)
        }
    return when (processorType) {
      ProcessorType.AP ->
          runProcessorTestWithoutKsp(sources) { invocation ->
            process(invocation, qualifiedName, assertions)
          }
      ProcessorType.KSP ->
          runKspTest(sources) { invocation -> process(invocation, qualifiedName, assertions) }
    }
  }

  private fun process(
      invocation: XTestInvocation,
      qualifiedName: String,
      assertions: (CompilerClass) -> Unit,
  ): CompilerClass {
    val env = invocation.processingEnv
    val typeElement =
        env.findTypeElement(qualifiedName)
            ?: throw IllegalStateException("No type element found for: $qualifiedName")
    val declaredType = env.getDeclaredType(typeElement)
    val compilerClass = CompilerClass(env, declaredType)
    assertions.invoke(compilerClass)
    invocation.assertCompilationResult { this.hasErrorCount(0) }
    return compilerClass
  }

  // Map the 1 multiline Java string to multiple source files so that types are properly resolved
  private fun createJavaSources(srcFiles: List<String>, pkg: String): List<Source> {
    return srcFiles.map { src ->
      val name =
          "(class|interface)\\s+\\w+".toRegex().find(src)?.value?.substringAfterLast(" ")
              ?: "Unknown"
      val imports =
          setOf("Foo", "Bar", "Baz")
              .filter { it != name && it in src }
              .map { "import $pkg.$it;" }
              .joinToString("\n")
      val code =
          """
        package $pkg;
        $imports

        $src
      """
              .trimIndent()
      return@map Source.java("$pkg.$name", code)
    }
  }

  // Map the 1 multiline Java string to multiple KT source files so that types are properly resolved
  private fun createKotlinSources(srcFiles: List<String>, pkg: String): List<Source> {
    return srcFiles
        .map { it.replace("implements", ":").replace("extends", ":").replace(";", "") }
        .map { src ->
          val name =
              "(class|interface)\\s+\\w+".toRegex().find(src)?.value?.substringAfterLast(" ")
                  ?: "Unknown"
          val imports =
              setOf("Foo", "Bar", "Baz")
                  .filter { it != name && it in src }
                  .map { "import $pkg.$it" }
                  .joinToString("\n")
          val hasParentClass =
              ":" in src &&
                  src.substringAfter(" : ").substringBeforeLast(" ").substringBefore("<").let {
                    srcFiles.any { src -> "class $it" in src }
                  }

          val code =
              """
          package $pkg
          import kotlin.String
          $imports

          ${if (hasParentClass) src.replace(" {}", "() {}") else src}
          """
                  .trimIndent()
          return@map Source.kotlin("$pkg/$name.kt", code)
        }
  }

  private fun String.toSourceFor(): String =
      if (srcLang == SourceLanguage.KOTLIN) {
        this
      } else {
        this.replace("open ", "")
      }

  private fun List<String>.forLang(): Array<String> =
      if (srcLang == SourceLanguage.KOTLIN) {
            map {
              when (it) {
                "java.lang.Object" -> "kotlin.Any"
                "java.lang.String" -> "kotlin.String"
                else -> it
              }
            }
          } else {
            this
          }
          .toTypedArray()
}

enum class ProcessorType {
  AP,
  KSP,
}

enum class SourceLanguage {
  JAVA,
  KOTLIN,
}
