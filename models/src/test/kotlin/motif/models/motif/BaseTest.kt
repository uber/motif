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
package motif.models.motif

import com.google.testing.compile.CompilationSubject.assertThat
import com.google.testing.compile.Compiler.javac
import com.google.testing.compile.JavaFileObjects
import motif.ast.IrClass
import motif.ast.compiler.CompilerClass
import motif.models.Scope
import org.intellij.lang.annotations.Language
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.tools.JavaFileObject

abstract class BaseTest {

    private val files: MutableList<JavaFileObject> = mutableListOf()

    fun addClass(qualifiedName: String, @Language("JAVA") classText: String) {
        files.add(JavaFileObjects.forSourceString(qualifiedName, classText))
    }

    fun getScopes(): List<Scope> {
        val processor = Processor()

        val compilation = javac()
                .withProcessors(processor)
                .compile(files)

        assertThat(compilation).succeeded()

        return Scope.fromClasses(processor.scopeClasses.toList())
                .sortedBy { scopeClass -> scopeClass.clazz.qualifiedName }
    }

    private class Processor : AbstractProcessor() {

        lateinit var scopeClasses: Set<IrClass>

        override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
            if (roundEnv.processingOver()) {
                return true
            }
            scopeClasses = roundEnv.getElementsAnnotatedWith(motif.Scope::class.java)
                    .map { CompilerClass(processingEnv, it.asType() as DeclaredType) }
                    .toSet()
            return true
        }

        override fun getSupportedSourceVersion(): SourceVersion {
            return SourceVersion.latestSupported()
        }

        override fun getSupportedAnnotationTypes(): Set<String> {
            return setOf(motif.Scope::class.java.name)
        }
    }
}
