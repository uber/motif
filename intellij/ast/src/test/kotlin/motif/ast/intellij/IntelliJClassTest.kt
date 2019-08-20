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

import com.intellij.psi.PsiElementFactory
import com.intellij.psi.PsiTypeVariable
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.Test

class IntelliJClassTest : LightCodeInsightFixtureTestCase() {

    lateinit var psiElementFactory: PsiElementFactory

    override fun setUp() {
        super.setUp()

        psiElementFactory = PsiElementFactory.SERVICE.getInstance(project)
    }

    override fun getTestDataPath(): String {
        return "testData"
    }

    fun testInheritedMethod() {
        val fooClass = createIntelliJClass("""
            package motif.intellij;

            interface Foo extends Bar {}

            interface Bar {
                String a();
            }
        """.trimIndent())

        assertThat(fooClass.methods).hasSize(1)
    }

    fun testInheritedMethod_generic() {
        val fooClass = createIntelliJClass("""
            package motif.intellij;

            interface Foo extends Bar<String> {}

            interface Bar<T> {
                T a(T t);
            }
        """.trimIndent())

        assertThat(fooClass.methods).hasSize(1)
        assertThat(fooClass.methods[0].returnType.qualifiedName).isEqualTo("java.lang.String")
        assertThat(fooClass.methods[0].parameters[0].type.qualifiedName).isEqualTo("java.lang.String")
    }

    fun testInheritedMethod_static() {
        val fooClass = createIntelliJClass("""
            package motif.intellij;

            class Foo extends Bar {}

            class Bar {
                static String a() {}
            }
        """.trimIndent())

        assertThat(fooClass.methods).hasSize(1)
    }

    fun testInheritedMethod_privateStatic() {
        val fooClass = createIntelliJClass("""
            package motif.intellij;

            class Foo extends Bar {}

            class Bar {
                private static String a() {}
            }
        """.trimIndent())

        assertThat(fooClass.methods).isEmpty()
    }

    fun testInheritedField() {
        val fooClass = createIntelliJClass("""
            package motif.intellij;

            class Foo extends Bar {}

            class Bar {
                String a;
            }
        """.trimIndent())

        assertThat(fooClass.fields).hasSize(1)
    }

    fun testInheritedField_generic() {
        val fooClass = createIntelliJClass("""
            package motif.intellij;

            class Foo extends Bar<String> {}

            class Bar<T> {
                T t;
            }
        """.trimIndent())

        assertThat(fooClass.fields).hasSize(1)

        // TODO The following currently fails. This should be fixed if and when Motif requires correct types for
        // inherited fields. Today we only care about existence of fields.
        // assertThat(barClass.fields[0].type.qualifiedName).isEqualTo("java.lang.String")
    }

    fun testIsAssignableTo() {
        val fooClass = createIntelliJClass("""
            package motif.intellij;

            class Foo extends motif.intellij.Bar {}
        """.trimIndent())

        val barClass = createIntelliJClass("""
            package motif.intellij;

            class Bar {}
        """.trimIndent())

        assertThat(fooClass.type.isAssignableTo(barClass.type)).isTrue()
    }

    fun testIsAssignableTo_genericType() {
        val fooClass = createIntelliJClass("""
            package motif.intellij;

            class Foo extends Bar<String> {}

            class Bar<T> {}
        """.trimIndent())

        val stringBarType = createIntelliJType("motif.intellij.Bar<String>")

        assertThat(fooClass.type.isAssignableTo(stringBarType)).isTrue()
    }

    fun testIsAssignableTo_genericType2() {
        val fooClass = createIntelliJClass("""
            package motif.intellij;

            class Foo extends Bar<String> {}

            class Bar<T> extends Baz<T> {}

            class Baz<T> {}
        """.trimIndent())

        val stringBazType = createIntelliJType("motif.intellij.Baz<String>")

        assertThat(fooClass.type.isAssignableTo(stringBazType)).isTrue()
    }

    fun testSupertypeTypeArguments() {
        val fooClass = createIntelliJClass("""
            package test;
            
            class Foo extends Bar<String> {}
            
            class Bar<T> {}
        """.trimIndent())

        val superClass = fooClass.superclass!!.resolveClass() as IntelliJClass
        assertThat(superClass.typeArguments.map { it.qualifiedName }).containsExactly("java.lang.String")
    }

    @Test
    fun testSupertypeTypeArguments_intermediateClass() {
        val fooClass = createIntelliJClass("""
            package test;
            
            class Foo extends Bar<String> {}
            
            class Bar<T> extends Baz<T> {}
            
            class Baz<T> {}
        """.trimIndent())

        val barClass = fooClass.superclass!!.resolveClass() as IntelliJClass
        val bazClass = barClass.superclass!!.resolveClass() as IntelliJClass
        assertThat(bazClass.typeArguments.map { it.qualifiedName }).containsExactly("java.lang.String")
    }

    fun testSupertypeTypeArguments_typeVariable() {
        val fooClass = createIntelliJClass("""
            package test;
            
            class Foo<T> extends Bar<T> {}
            
            class Bar<T> {}
        """.trimIndent())

        val superClass = fooClass.superclass!!.resolveClass() as IntelliJClass
        assertThat(superClass.typeArguments).isEmpty()
    }

    fun testObjectSuperclass() {
        val fooClass = createIntelliJClass("""
            package test;
            
            class Foo {}
        """.trimIndent())

        val objectClass = fooClass.superclass!!.resolveClass()!!
        assertThat(objectClass.qualifiedName).isEqualTo("java.lang.Object")
        assertThat(objectClass.superclass).isNull()
    }

    fun testSuperclassOnlyInterface() {
        val fooClass = createIntelliJClass("""
            package test;
            
            class Foo implements Bar {}
            
            interface Bar {}
        """.trimIndent())

        val objectClass = fooClass.superclass!!.resolveClass()!!
        assertThat(objectClass.qualifiedName).isEqualTo("java.lang.Object")
        assertThat(objectClass.superclass).isNull()
    }

    private fun createIntelliJClass(@Language("JAVA") classText: String): IntelliJClass {
        val psiClass = myFixture.addClass(classText)
        return IntelliJClass(project, psiElementFactory.createType(psiClass))
    }

    private fun createIntelliJType(classText: String): IntelliJType {
        val psiType = elementFactory.createTypeFromText(classText, null)
        return IntelliJType(project, psiType)
    }
}
