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
package motif.ast.compiler

import com.google.common.truth.Truth.assertAbout
import com.google.common.truth.Truth.assertThat
import com.google.testing.compile.JavaFileObjects
import com.google.testing.compile.JavaSourceSubjectFactory.javaSource
import org.intellij.lang.annotations.Language
import org.junit.Test
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement

class CompilerClassTest {

    @Test
    fun testSupertypeTypeArguments() {
        val fooClass = createClass("test.Foo", """
            package test;
            
            class Bar<T> {}
            
            class Foo extends Bar<String> {}
        """.trimIndent())

        val superClass = fooClass.supertypes.single().resolveClass() as CompilerClass
        assertThat(superClass.typeArguments.map { it.qualifiedName }).containsExactly("java.lang.String")
    }

    @Test
    fun testSupertypeTypeArguments_intermediateClass() {
        val fooClass = createClass("test.Foo", """
            package test;
            
            class Baz<T> {}
            
            class Bar<T> extends Baz<T> {}
            
            class Foo extends Bar<String> {}
        """.trimIndent())

        val barClass = fooClass.supertypes.single().resolveClass() as CompilerClass
        val bazClass = barClass.supertypes.single().resolveClass() as CompilerClass
        assertThat(bazClass.typeArguments.map { it.qualifiedName }).containsExactly("java.lang.String")
    }

    @Test
    fun testSupertypeTypeArguments_typeVariable() {
        val fooClass = createClass("test.Foo", """
            package test;
            
            class Bar<T> {}
            
            class Foo<T> extends Bar<T> {}
        """.trimIndent())

        val superClass = fooClass.supertypes.single().resolveClass() as CompilerClass
        assertThat(superClass.typeArguments).isEmpty()
    }

    @Test
    fun testObjectSupertype() {
        val fooClass = createClass("test.Foo", """
            package test;
            
            class Foo {}
        """.trimIndent())

        val objectClass = fooClass.supertypes.single().resolveClass()!!
        assertThat(objectClass.qualifiedName).isEqualTo("java.lang.Object")
        assertThat(objectClass.supertypes).isEmpty()
    }

    @Test
    fun testSupertypeOnlyInterface() {
        val fooClass = createClass("test.Foo", """
            package test;
            
            interface Bar {}
            
            class Foo implements Bar {}
        """.trimIndent())

        val superTypes = fooClass.supertypes.map { it.qualifiedName }
        assertThat(superTypes).containsExactly("java.lang.Object", "test.Bar")
    }

    @Test
    fun testSupertypeMultipleInterfaces() {
        val fooClass = createClass("test.Foo", """
            package test;
            
            interface Bar {}
            
            interface Baz {}
            
            class Foo implements Bar, Baz {}
        """.trimIndent())

        val superTypes = fooClass.supertypes.map { it.qualifiedName }
        assertThat(superTypes).containsExactly("java.lang.Object", "test.Bar", "test.Baz")
    }

    private fun createClass(qualifiedName: String, @Language("JAVA") text: String): CompilerClass {
        var compilerClass: CompilerClass? = null
        assertAbout(javaSource())
                .that(JavaFileObjects.forSourceString(qualifiedName, text))
                .processedWith(object : AbstractProcessor() {

                    override fun getSupportedAnnotationTypes(): Set<String> {
                        return setOf("*")
                    }

                    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
                        val typeElement = processingEnv.elementUtils.getTypeElement(qualifiedName)
                        val declaredType = processingEnv.typeUtils.getDeclaredType(typeElement)
                        compilerClass = CompilerClass(processingEnv, declaredType)
                        return false
                    }
                })
                .compilesWithoutError()

        assertThat(compilerClass).isNotNull()
        return compilerClass!!
    }
}