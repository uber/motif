package com.uber.motif.compiler.codegen

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeSpec
import com.uber.motif.compiler.graph.ResolvedScope

class DaggerScopeSpec(resolvedScope: ResolvedScope) {

    val className: ClassName = resolvedScope.scopeImplName.nestedClass("Scope")
    val spec: TypeSpec = TypeSpec.annotationBuilder(className)
            .addAnnotation(javax.inject.Scope::class.java)
            .build()
}