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
package motif.compiler

import androidx.room.compiler.processing.ExperimentalProcessingApi
import androidx.room.compiler.processing.util.Source
import androidx.room.compiler.processing.util.XTestInvocation
import androidx.room.compiler.processing.util.runKspTest
import androidx.room.compiler.processing.util.runProcessorTestWithoutKsp
import com.google.common.collect.Sets.cartesianProduct
import com.google.common.truth.Truth.assertThat
import com.squareup.javapoet.ClassName
import javax.inject.Qualifier
import motif.compiler.Names.Companion.safeName
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
@OptIn(ExperimentalProcessingApi::class)
class XNamesTest(private val processorType: ProcessorType, private val srcLang: SourceLanguage) {

  companion object {
    @JvmStatic
    @Parameterized.Parameters(name = "{0}_{1}")
    fun data(): Collection<Array<Any>> {
      return cartesianProduct(
              ProcessorType.values().toSortedSet(), SourceLanguage.values().toSortedSet())
          .filterNot { (proc, srcLang) ->
            proc == ProcessorType.AP && srcLang == SourceLanguage.KOTLIN
          }
          .map { it.toTypedArray() as Array<Any> }
          .toList()
    }
  }

  @Test
  fun primitive() {
    assertSafeName("int", "kotlin.Int", "integer")
  }

  @Test
  fun boxed() {
    assertSafeName("java.lang.Integer", "kotlin.Int?", "integer")
  }

  @Test
  fun raw() {
    assertSafeName("java.util.HashMap", "java.util.HashMap<*, *>", "hashMap")
  }

  @Test
  fun typeArgument() {
    assertSafeName(
        "java.util.HashMap<String, Integer>",
        "java.util.HashMap<String, Integer>",
        "stringIntegerHashMap")
  }

  @Test
  fun wildcard() {
    assertSafeName(
        "java.util.HashMap<? extends String, ? super Integer>",
        "java.util.HashMap<out String, out Integer>",
        "stringIntegerHashMap")
  }

  @Test
  fun wildcardUnbounded() {
    assertSafeName("java.util.HashMap<?, ?>", "java.util.HashMap<*, *>", "hashMap")
  }

  @Test
  fun typeVariable() {
    assertSafeName("java.util.HashMap<String, A>", "java.util.HashMap<String, A>", "stringAHashMap")
  }

  @Test
  fun typeVariableUnbounded() {
    assertSafeName("java.util.HashMap<String, B>", "java.util.HashMap<String, B>", "stringBHashMap")
  }

  @Test
  fun nested() {
    assertSafeName(
        "java.util.HashMap<java.util.HashMap<String, Integer>, Integer>",
        "java.util.HashMap<java.util.HashMap<String, Integer>, Integer>",
        "stringIntegerHashMapIntegerHashMap")
  }

  @Test
  fun innerClass() {
    assertSafeName(
        "java.util.Map.Entry<String, Integer>",
        "java.util.Map.Entry<String, Integer>",
        "stringIntegerMapEntry")
  }

  @Test
  fun keyword() {
    assertSafeName("java.lang.Boolean", "java.lang.Boolean", "boolean_")
  }

  @Test
  fun named() {
    assertSafeName(
        "String", "String", "fooString", "@javax.inject.Named(\"Foo\")")
  }

  @Test
  fun customQualifier() {
    assertSafeName("String", "String", "fooString", qualifierString = "@Foo")
  }

  @Test
  fun array() {
    assertSafeName("String[]", "Array<String>", "stringArray")
  }

  @Test
  fun enum() {
    assertSafeName("LogLevel", "LogLevel", "logLevel")
  }

  @Test
  fun errorType() {
    assertErrorMessage("DoesNotExist", "DoesNotExist")
  }

  @Test
  fun errorType_typeArgument() {
    assertErrorMessage("java.util.HashMap<DoesNotExist, Integer>", "DoesNotExist")
  }

  private fun compile(classString: String, qualifierString: String, assertion: (XTestInvocation, String) -> Unit) {
    when (processorType) {
      ProcessorType.AP -> {
        runProcessorTestWithoutKsp(sources = getSources(classString, qualifierString)) { invocation ->
          val safeName = process(invocation)
          assertion(invocation, safeName)
        }
      }
      ProcessorType.KSP -> {
        runKspTest(sources = getSources(classString, qualifierString)) { invocation ->
          val safeName = process(invocation)
          assertion(invocation, safeName)
        }
      }
    }
  }

  private fun process(invocation: XTestInvocation): String {
    val env = invocation.processingEnv
    val typeElement = env.findTypeElement("test.Test")
    assertThat(typeElement).isNotNull()
    val methods = typeElement?.getDeclaredMethods().orEmpty()
    assertThat(methods).isNotEmpty()
    val testMethod = methods[0]
    val returnType = testMethod.returnType
    val qualifiers = testMethod.getAnnotationsAnnotatedWith(ClassName.get(Qualifier::class.java))
    val qualifier = if (qualifiers.isEmpty()) null else qualifiers.iterator().next()
    return safeName(returnType, qualifier)
  }

  private fun getSources(classString: String, qualifierString: String = ""): List<Source> {
    val contentJava =
        """
                package test;
                import javax.inject.Qualifier;

                @javax.inject.Qualifier @interface Foo {}

                enum LogLevel {
                    INFO,
                    ERROR
                }

                interface Test<A extends String, B> {
                    $qualifierString
                    $classString test();
                }
            """
    val contentKotlin =
        """
                package test
                import javax.inject.Qualifier

                @Qualifier annotation class Foo

                enum class LogLevel {
                    INFO,
                    ERROR
                }

                interface Test<A : String, B : Any> {
                    $qualifierString
                    fun test(): $classString
                }
            """
    val source =
        when (srcLang) {
          SourceLanguage.JAVA -> Source.java("test.Test", contentJava)
          SourceLanguage.KOTLIN -> Source.kotlin("test/Test.kt", contentKotlin)
        }
    return listOf(source)
  }

  private fun assertErrorMessage(classString: String, expectedError: String) {
    val assertion = { invocation: XTestInvocation, _: String ->
      invocation.assertCompilationResult {
        hasError()
        hasErrorContaining(expectedError)
      }
    }
    compile(classString, "", assertion)
  }

  private fun assertSafeName(
      javaClassString: String,
      ktClassString: String,
      expectedSafeName: String,
      qualifierString: String = ""
  ) {
    val assertion = { invocation: XTestInvocation, safeName: String ->
      invocation.assertCompilationResult {
        hasErrorCount(0)
        assertThat(safeName).isEqualTo(expectedSafeName)
      }
    }
    val classString =
        when (srcLang) {
          SourceLanguage.JAVA -> javaClassString
          SourceLanguage.KOTLIN -> ktClassString
        }
    compile(classString, qualifierString, assertion)
  }
}
