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
import com.squareup.javapoet.JavaFile
import motif.Scope
import motif.ast.compiler.CompilerClass
import motif.core.ResolvedGraph
import motif.errormessage.ErrorMessage
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.type.DeclaredType
import javax.tools.Diagnostic

const val OPTION_KAPT_KOTLIN_GENERATED = "kapt.kotlin.generated"
const val OPTION_MODE = "motif.mode"

enum class Mode {
    DAGGER, // Generate Dagger-based (Java) implementation
    JAVA,   // Generate pure Java implementation
    KOTLIN, // Generate pure Kotlin implementation
}

class Processor : BasicAnnotationProcessor() {

    lateinit var graph: ResolvedGraph

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun initSteps(): Iterable<ProcessingStep> {
        return listOf<ProcessingStep>(Step())
    }

    override fun getSupportedOptions(): Set<String> {
        return setOf(OPTION_MODE, OPTION_KAPT_KOTLIN_GENERATED)
    }

    private inner class Step : ProcessingStep {

        override fun annotations(): Set<Class<out Annotation>> {
            return setOf(motif.Scope::class.java)
        }

        override fun process(elementsByAnnotation: SetMultimap<Class<out Annotation>, Element>): Set<Element> {
            val scopeElements = elementsByAnnotation[Scope::class.java]
            val initialScopeClasses = scopeElements
                    .map { CompilerClass(processingEnv, it.asType() as DeclaredType) }
            if (initialScopeClasses.isEmpty()) {
                return emptySet()
            }

            this@Processor.graph = ResolvedGraph.create(initialScopeClasses)

            if (graph.errors.isNotEmpty()) {
                val errorMessage = ErrorMessage.toString(graph)
                processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, errorMessage)
                return emptySet()
            }

            val mode: Mode? = try {
                Mode.valueOf(processingEnv.options[OPTION_MODE]?.toUpperCase() ?: "")
            } catch (ignore: IllegalArgumentException) {
                null
            }

            val generate: (mode: Mode?, graph: ResolvedGraph) -> Unit = if (mode == Mode.DAGGER) {
                this@Processor::generateDagger
            } else {
                this@Processor::generateV2
            }

            generate(mode, graph)

            return emptySet()
        }
    }

    private fun generateDagger(mode: Mode?, graph: ResolvedGraph) {
        val generatedClasses = CodeGenerator.generate(processingEnv, graph)

        generatedClasses.forEach { generatedClass ->
            JavaFile.builder(generatedClass.packageName, generatedClass.spec).build().writeTo(processingEnv.filer)
        }
    }

    private fun generateV2(mode: Mode?, graph: ResolvedGraph) {
        motif.compiler.codegenv2.CodeGenerator.generate(processingEnv, graph, mode)
    }
}
