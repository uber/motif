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
import javax.lang.model.type.TypeKind

class CompilerClassTest {

    @Test
    fun testSupertypeTypeArguments() {
        val fooClass = createClass("test.Foo", """
            package test;
            
            class Bar<T> {}
            
            class Foo extends Bar<String> {}
        """.trimIndent())

        val superClass = fooClass.superclass.resolveClass() as CompilerClass
        assertThat(superClass.typeArguments.map { it.qualifiedName }).containsExactly("java.lang.String")
    }

    @Test
    fun testSupertypeTypeArguments_typeVariable() {
        val fooClass = createClass("test.Foo", """
            package test;
            
            class Bar<T> {}
            
            class Foo<T> extends Bar<T> {}
        """.trimIndent())

        val superClass = fooClass.superclass.resolveClass() as CompilerClass
        val typeArgument = superClass.typeArguments.single() as CompilerType
        assertThat(typeArgument.mirror.kind).isSameInstanceAs(TypeKind.TYPEVAR)
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