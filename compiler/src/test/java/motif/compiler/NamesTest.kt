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
package motif.compiler

import com.google.common.collect.ImmutableSet
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode.COMPILATION_ERROR
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode.INTERNAL_ERROR
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode.OK
import com.tschuchort.compiletesting.SourceFile.Companion.java
import dagger.shaded.auto.common.AnnotationMirrors
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.inject.Qualifier
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.util.ElementFilter
import motif.compiler.Names.Companion.safeName
import org.junit.Test

class NamesTest {

  @Test
  fun basic() {
    val name = getSafeName("java.util.HashMap")
    assertThat(name).isEqualTo("hashMap")
  }

  @Test
  fun typeArgument() {
    val name = getSafeName("java.util.HashMap<String, Integer>")
    assertThat(name).isEqualTo("stringIntegerHashMap")
  }

  @Test
  fun wildcard() {
    val name = getSafeName("java.util.HashMap<? extends String, ? super Integer>")
    assertThat(name).isEqualTo("stringIntegerHashMap")
  }

  @Test
  fun typeVariable() {
    val name = getSafeName("java.util.HashMap<String, A>")
    assertThat(name).isEqualTo("stringAHashMap")
  }

  @Test
  fun nested() {
    val name = getSafeName("java.util.HashMap<java.util.HashMap<String, Integer>, Integer>")
    assertThat(name).isEqualTo("stringIntegerHashMapIntegerHashMap")
  }

  @Test
  fun innerClass() {
    val name = getSafeName("java.util.Map.Entry<String, Integer>")
    assertThat(name).isEqualTo("stringIntegerMapEntry")
  }

  @Test
  fun keyword() {
    val name = getSafeName("java.lang.Boolean")
    assertThat(name).isEqualTo("boolean_")
  }

  @Test
  fun named() {
    val name = getSafeName("@javax.inject.Named(\"Foo\") String")
    assertThat(name).isEqualTo("fooString")
  }

  @Test
  fun customQualifier() {
    val name = getSafeName("@Foo String")
    assertThat(name).isEqualTo("fooString")
  }

  @Test
  fun errorType() {
    val errorMessage = getErrorMessage("DoesNotExist")
    assertThat(errorMessage).contains("DoesNotExist")
  }

  @Test
  fun errorType_typeArgument() {
    val errorMessage = getErrorMessage("java.util.HashMap<DoesNotExist, Integer>")
    assertThat(errorMessage).contains("DoesNotExist")
  }

  private class SafeNameProcessor : AbstractProcessor() {
    lateinit var safeName: String

    override fun getSupportedAnnotationTypes(): Set<String> = ImmutableSet.of("*")

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
      if (roundEnv.processingOver()) {
        return true
      }
      val typeElement = processingEnv.elementUtils.getTypeElement("test.Test")
      assertThat(typeElement).isNotNull()
      val methods = ElementFilter.methodsIn(typeElement.enclosedElements)
      assertThat(methods).isNotEmpty()
      val testMethod = methods[0]
      val returnType = testMethod.returnType
      val qualifiers = AnnotationMirrors.getAnnotatedAnnotations(testMethod, Qualifier::class.java)
      val qualifier = if (qualifiers.isEmpty()) null else qualifiers.iterator().next()
      safeName = safeName(returnType, qualifier)
      return true
    }
  }

  private fun compile(processor: Processor, classString: String): KotlinCompilation.Result {
    val content =
        """
                package test;
                import javax.inject.Qualifier;

                @javax.inject.Qualifier @interface Foo {}

                class Test<A extends String> {
                    $classString  test() { return null; }
                }
            """
    return KotlinCompilation()
        .apply {
          inheritClassPath = true
          annotationProcessors = listOf(processor)
          sources = listOf(java("Test.java", content, true))
        }
        .compile()
  }

  private fun getErrorMessage(classString: String): String {
    val result = compile(SafeNameProcessor(), classString)
    if (result.exitCode != COMPILATION_ERROR && result.exitCode != INTERNAL_ERROR) {
      assertWithMessage(result.messages).fail()
    }
    return result.messages
  }

  private fun getSafeName(classString: String): String {
    val processor = SafeNameProcessor()
    val result = compile(processor, classString)
    if (result.exitCode != OK) {
      assertWithMessage(result.messages).fail()
    }
    return processor.safeName
  }
}
