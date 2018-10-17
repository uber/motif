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
package motif.compiler

import motif.Scope
import motif.compiler.codegen.Generator
import motif.compiler.errors.ErrorHandler
import motif.compiler.ir.CompilerType
import motif.models.errors.MotifErrors
import motif.models.graph.GraphFactory
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

class Processor : AbstractProcessor() {

    // For testing only.
    var errors: MotifErrors? = null

    private val hasErrors: Boolean
        get() = !(errors?.isEmpty() ?: true)

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return setOf(Scope::class.java.name)
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        if (roundEnv.processingOver() || hasErrors) {
            return true
        }
        process(roundEnv)
        return true
    }

    private fun process(roundEnv: RoundEnvironment) {
        val scopeTypes = roundEnv.getElementsAnnotatedWith(Scope::class.java)
                .map { CompilerType(processingEnv, it.asType()) }
                .toSet()
        val graph = GraphFactory.create(scopeTypes)
        val errors = graph.errors
        this.errors = errors

        if (errors.isEmpty()) {
            Generator(processingEnv, graph).generate()
        } else {
            errors.forEach { error ->
                val errorMessage = ErrorHandler.handle(error)
                processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "\n${errorMessage.message}\n", errorMessage.element)
            }
        }
    }
}