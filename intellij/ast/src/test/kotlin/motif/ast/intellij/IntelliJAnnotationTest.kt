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
package motif.ast.intellij

import com.intellij.openapi.projectRoots.impl.JavaAwareProjectJdkTableImpl
import com.intellij.psi.PsiElementFactory
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language

class IntelliJAnnotationTest : LightJavaCodeInsightFixtureTestCase() {

  lateinit var psiElementFactory: PsiElementFactory

  override fun setUp() {
    super.setUp()
    psiElementFactory = PsiElementFactory.SERVICE.getInstance(project)
  }

  override fun getProjectDescriptor() = DefaultLightProjectDescriptor {
    JavaAwareProjectJdkTableImpl.getInstanceEx().internalJdk
  }

  override fun getTestDataPath(): String {
    return "testData"
  }

  fun testEqual() {
    createAnnotationClass(
        """
            package test;

            @interface A {}
      """.trimIndent())

    val fooAnnotation =
        getClassAnnotation(
            """
            package test;

            @A class Foo {}
        """.trimIndent())

    val barAnnotation =
        getClassAnnotation(
            """
            package test;

            @A class Bar {}
        """.trimIndent())

    assertThat(fooAnnotation).isEqualTo(barAnnotation)
  }

  fun testNotEqual() {
    createAnnotationClass(
        """
            package test;

            @interface A {}
      """.trimIndent())

    createAnnotationClass(
        """
            package test;

            @interface B {}
      """.trimIndent())

    val fooAnnotation =
        getClassAnnotation(
            """
            package test;

            @A class Foo {}
        """.trimIndent())

    val barAnnotation =
        getClassAnnotation(
            """
            package test;

            @B class Bar {}
        """.trimIndent())

    assertThat(fooAnnotation).isNotEqualTo(barAnnotation)
  }

  fun testEqual_qualified() {
    createAnnotationClass(
        """
            package test;

            @interface A {}
      """.trimIndent())

    val fooAnnotation =
        getClassAnnotation(
            """
            package test;

            @test.A class Foo {}
        """.trimIndent())

    val barAnnotation =
        getClassAnnotation(
            """
            package test;

            @A class Bar {}
        """.trimIndent())

    assertThat(fooAnnotation).isEqualTo(barAnnotation)
  }

  fun testEqual_named() {
    val fooAnnotation =
        getClassAnnotation(
            """
            package test;

            @javax.inject.Named("a")
            class Foo {}
        """.trimIndent())

    val barAnnotation =
        getClassAnnotation(
            """
            package test;

            @javax.inject.Named("a")
            class Bar {}
        """.trimIndent())

    assertThat(fooAnnotation).isEqualTo(barAnnotation)
  }

  fun testNotEqual_named() {
    val fooAnnotation =
        getClassAnnotation(
            """
            package test;

            @javax.inject.Named("a")
            class Foo {}
        """.trimIndent())

    val barAnnotation =
        getClassAnnotation(
            """
            package test;

            @javax.inject.Named("b")
            class Bar {}
        """.trimIndent())

    assertThat(fooAnnotation).isNotEqualTo(barAnnotation)
  }

  fun testEqual_namedReference() {
    val fooAnnotation =
        getClassAnnotation(
            """
            package test;

            @javax.inject.Named(Foo.S)
            class Foo {
              static final String S = "a";
            }
        """.trimIndent())

    val barAnnotation =
        getClassAnnotation(
            """
            package test;

            @javax.inject.Named(Bar.S)
            class Bar {
              static final String S = "a";
            }
        """.trimIndent())

    assertThat(fooAnnotation).isEqualTo(barAnnotation)
  }

  fun testNotEqual_namedReference() {
    val fooAnnotation =
        getClassAnnotation(
            """
            package test;

            @javax.inject.Named(Foo.S)
            class Foo {
              static final String S = "a";
            }
        """.trimIndent())

    val barAnnotation =
        getClassAnnotation(
            """
            package test;

            @javax.inject.Named(Bar.S)
            class Bar {
              static final String S = "b";
            }
        """.trimIndent())

    assertThat(fooAnnotation).isNotEqualTo(barAnnotation)
  }

  fun testEqual_namedReferenceAndString() {
    val fooAnnotation =
        getClassAnnotation(
            """
            package test;

            @javax.inject.Named("a")
            class Foo {}
        """.trimIndent())

    val barAnnotation =
        getClassAnnotation(
            """
            package test;

            @javax.inject.Named(Bar.S)
            class Bar {
              static final String S = "a";
            }
        """.trimIndent())

    assertThat(fooAnnotation).isEqualTo(barAnnotation)
  }

  fun testNotEqual_namedReferenceAndString() {
    val fooAnnotation =
        getClassAnnotation(
            """
            package test;

            @javax.inject.Named("a")
            class Foo {}
        """.trimIndent())

    val barAnnotation =
        getClassAnnotation(
            """
            package test;

            @javax.inject.Named(Bar.S)
            class Bar {
              static final String S = "b";
            }
        """.trimIndent())

    assertThat(fooAnnotation).isNotEqualTo(barAnnotation)
  }

  fun testClassName() {
    val fooAnnotation =
        getClassAnnotation(
            """
            package test;

            @javax.inject.Named("a")
            class Foo {}
        """.trimIndent())

    assertThat(fooAnnotation.className).isEqualTo("javax.inject.Named")
  }

  private fun createAnnotationClass(@Language("JAVA") classText: String): IntelliJClass {
    val psiClass = myFixture.addClass(classText)
    return IntelliJClass(project, psiElementFactory.createType(psiClass), psiClass)
  }

  private fun getClassAnnotation(@Language("JAVA") classText: String): IntelliJAnnotation {
    val psiClass = myFixture.addClass(classText)
    val psiAnnotation = psiClass.annotations.single()
    return IntelliJAnnotation(project, psiAnnotation)
  }
}
