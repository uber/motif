/*
 * Copyright (c) 2018 Uber Technologies, Inc.
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
package motif.intellij.validation.ir

import com.google.common.truth.Truth.assertThat
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElementFactory
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase
import motif.models.java.IrField
import motif.models.java.IrMethod

class IrTest : LightCodeInsightFixtureTestCase() {

    lateinit var psiElementFactory: PsiElementFactory

    override fun setUp() {
        super.setUp()

        psiElementFactory = PsiElementFactory.SERVICE.getInstance(project)
    }

    fun testInheritedMethod() {
        val psiClass: PsiClass = myFixture.addClass("""
            package motif.intellij;

            interface Foo extends Bar {}

            interface Bar {
                String a();
            }
        """.trimIndent())

        val methods: List<IrMethod> = IntelliJClass(project, psiElementFactory.createType(psiClass)).methods
        assertThat(methods).hasSize(1)
    }

    fun testInheritedMethod_generic() {
        val psiClass: PsiClass = myFixture.addClass("""
            package motif.intellij;

            interface Foo extends Bar<String> {}

            interface Bar<T> {
                T a(T t);
            }
        """.trimIndent())

        val methods: List<IrMethod> = IntelliJClass(project, psiElementFactory.createType(psiClass)).methods
        assertThat(methods).hasSize(1)
        assertThat(methods[0].returnType.qualifiedName).isEqualTo("java.lang.String")
        assertThat(methods[0].parameters[0].type.qualifiedName).isEqualTo("java.lang.String")
    }

    fun testInheritedMethod_static() {
        val psiClass: PsiClass = myFixture.addClass("""
            package motif.intellij;

            class Foo extends Bar {}

            class Bar {
                static String a() {}
            }
        """.trimIndent())

        val methods: List<IrMethod> = IntelliJClass(project, psiElementFactory.createType(psiClass)).methods
        assertThat(methods).hasSize(1)
    }

    fun testInheritedMethod_privateStatic() {
        val psiClass: PsiClass = myFixture.addClass("""
            package motif.intellij;

            class Foo extends Bar {}

            class Bar {
                private static String a() {}
            }
        """.trimIndent())

        val methods: List<IrMethod> = IntelliJClass(project, psiElementFactory.createType(psiClass)).methods
        assertThat(methods).isEmpty()
    }

    fun testInheritedField() {
        val psiClass: PsiClass = myFixture.addClass("""
            package motif.intellij;

            class Foo extends Bar {}

            class Bar {
                String a;
            }
        """.trimIndent())

        val fields: List<IrField> = IntelliJClass(project, psiElementFactory.createType(psiClass)).fields
        assertThat(fields).hasSize(1)
    }

    fun testInheritedField_generic() {
        val psiClass: PsiClass = myFixture.addClass("""
            package motif.intellij;

            class Foo extends Bar<String> {}

            class Bar<T> {
                T a;
            }
        """.trimIndent())

        val fields: List<IrField> = IntelliJClass(project, psiElementFactory.createType(psiClass)).fields
        assertThat(fields).hasSize(1)

        // TODO The following currently fails. This should be fixed if and when Motif requires correct types for
        // inherited fields. Today we only care about existence of fields.
        // assertThat(fields[0].type.qualifiedName).isEqualTo("java.lang.String")
    }
}