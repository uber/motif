package com.uber.motif.compiler

import com.squareup.javapoet.JavaFile
import com.uber.motif.Scope
import com.uber.motif.compiler.codegen.ScopeImplSpec
import com.uber.motif.compiler.graph.ResolvedGraph
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType

class AnnotationProcessor : AbstractProcessor() {

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return setOf(Scope::class.java.name)
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val scopes = roundEnv.getElementsAnnotatedWith(Scope::class.java)
                .map { it as TypeElement }
                .map { it.asType() as DeclaredType }
        val graph = ResolvedGraph.resolve(processingEnv, scopes)
        graph.resolvedScopes.forEach {
            val scopeImpl = ScopeImplSpec(it)
            JavaFile.builder(it.packageName, scopeImpl.spec).build().writeTo(processingEnv.filer)
        }
        return false
    }
}