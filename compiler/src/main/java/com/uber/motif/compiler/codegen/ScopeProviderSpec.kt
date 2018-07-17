package com.uber.motif.compiler.codegen

import com.squareup.javapoet.MethodSpec
import com.uber.motif.compiler.graph.ResolvedScope
import dagger.Provides

class ScopeProviderSpec(
        resolvedScope: ResolvedScope,
        daggerScope: DaggerScopeSpec,
        internalQualifier: InternalQualifierSpec,
        name: String) {

    val spec: MethodSpec = MethodSpec.methodBuilder(name)
            .addAnnotation(internalQualifier.className)
            .addAnnotation(daggerScope.className)
            .addAnnotation(Provides::class.java)
            .returns(resolvedScope.scopeName)
            .addStatement("return \$T.this", resolvedScope.scopeImplName)
            .build()
}