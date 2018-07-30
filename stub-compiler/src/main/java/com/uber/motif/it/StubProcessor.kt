package com.uber.motif.it

import com.google.auto.common.MoreElements
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import com.uber.motif.Scope
import com.uber.motif.compiler.asDeclaredType
import com.uber.motif.compiler.graph.ClassNames
import com.uber.motif.compiler.methods
import java.util.*
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType

class StubProcessor : AbstractProcessor() {

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return Collections.singleton(Scope::class.java.name)
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        roundEnv.getElementsAnnotatedWith(Scope::class.java)
                .map { it as TypeElement }
                .forEach { scopeElement ->
                    val packageName: String = MoreElements.getPackage(scopeElement).qualifiedName.toString()
                    val spec: TypeSpec = spec(scopeElement.asDeclaredType())
                    JavaFile.builder(packageName, spec).build().writeTo(processingEnv.filer)
                }
        return true
    }

    private fun spec(scopeElement: DeclaredType): TypeSpec {
        val scopeClassName = ClassName.get(scopeElement)
        val scopeImplClassName = ClassNames.scopeImpl(scopeElement)
        val builder = TypeSpec.classBuilder(scopeImplClassName)
                .addSuperinterface(scopeClassName)

        scopeElement.methods(processingEnv)
                .forEach { method: ExecutableElement ->
                    val methodSpec: MethodSpec = MethodSpec.overriding(method, scopeElement, processingEnv.typeUtils)
                            .addStatement("throw new \$T()", IllegalStateException::class.java)
                            .build()
                    builder.addMethod(methodSpec)
                }

        return builder.build()
    }
}