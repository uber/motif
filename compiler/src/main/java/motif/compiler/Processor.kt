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

import com.google.auto.common.BasicAnnotationProcessor
import com.google.common.collect.SetMultimap
import motif.Scope
import motif.ast.compiler.CompilerType
import motif.compiler.codegen.Generator
import motif.compiler.errors.ErrorHandler
import motif.models.errors.MotifErrors
import motif.models.graph.GraphFactory
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class Processor : BasicAnnotationProcessor() {

    // For testing only.
    var errors: MotifErrors? = null

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun initSteps(): Iterable<BasicAnnotationProcessor.ProcessingStep> {
        return listOf<BasicAnnotationProcessor.ProcessingStep>(Step())
    }

    private inner class Step : BasicAnnotationProcessor.ProcessingStep {

        override fun annotations(): Set<Class<out Annotation>> {
            return setOf(motif.Scope::class.java)
        }

        override fun process(elementsByAnnotation: SetMultimap<Class<out Annotation>, Element>): Set<Element> {
            val scopeTypes = elementsByAnnotation[Scope::class.java]
                    .map { CompilerType(processingEnv, it.asType()) }
                    .toSet()
            val graph = GraphFactory.create(scopeTypes)
            val errors = graph.errors
            this@Processor.errors = errors

            if (errors.isEmpty()) {
                Generator(processingEnv, graph).generate()
            } else {
                errors.forEach { error ->
                    val errorMessage = ErrorHandler.handle(error)
                    processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "\n${errorMessage.message}\n", errorMessage.element)
                }
            }

            return emptySet()
        }
    }
}
