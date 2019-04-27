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

import com.squareup.javapoet.JavaFile
import motif.ast.compiler.CompilerClass
import motif.core.ResolvedGraph
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.tools.Diagnostic

class Processor : AbstractProcessor() {

    lateinit var graph: ResolvedGraph

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return setOf(motif.Scope::class.java.name)
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        if (roundEnv.processingOver()) {
            return true
        }
        process(roundEnv)
        return true
    }

    private fun process(roundEnv: RoundEnvironment) {
        val initialScopeClasses = roundEnv.getElementsAnnotatedWith(motif.Scope::class.java)
                .map { CompilerClass(processingEnv, it.asType() as DeclaredType) }
        if (initialScopeClasses.isEmpty()) {
            return
        }

        this.graph = ResolvedGraph.create(initialScopeClasses)

        if (graph.errors.isNotEmpty()) {
            // TODO Handle ProcessingErrors.
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Failed: ${graph.errors}")
            return
        }

        val scopeImpls = ScopeImpl.create(processingEnv, graph)

        scopeImpls.forEach { scopeImpl ->
            val spec = scopeImpl.spec ?: return@forEach
            JavaFile.builder(scopeImpl.packageName, spec).build().writeTo(processingEnv.filer)
        }
    }
}