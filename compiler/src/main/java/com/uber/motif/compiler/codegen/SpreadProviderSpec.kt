package com.uber.motif.compiler.codegen

import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.uber.motif.compiler.graph.ResolvedScope
import com.uber.motif.compiler.isPublic
import com.uber.motif.compiler.model.ProviderMethod
import com.uber.motif.compiler.names.UniqueNameSet
import dagger.Provides

class SpreadProviderSpec(
        private val methodNames: UniqueNameSet,
        private val resolvedScope: ResolvedScope,
        private val daggerScope: DaggerScopeSpec,
        private val internalQualifier: InternalQualifierSpec,
        private val providerMethod: ProviderMethod) {

    val specs: List<MethodSpec> = providerMethod.spreadDependencies.map { (dependency, method) ->
        MethodSpec.methodBuilder(methodNames.unique(dependency.preferredName)).apply {
            addAnnotation(Provides::class.java)
            addAnnotation(daggerScope.className)
            internalQualifier.annotationSpec(dependency, !providerMethod.method.isPublic)?.let { addAnnotation(it) }
            returns(dependency.className)
            addParameter(ParameterSpec.builder(providerMethod.providedDependency.className, "spread").apply {
                val isInternal = providerMethod.providedDependency !in resolvedScope.scope.providedPublicDependencies
                internalQualifier.annotationSpec(providerMethod.providedDependency, isInternal)?.let { addAnnotation(it) }
            }.build())
            addStatement("return spread.\$N()", method.simpleName.toString())
        }.build()
    }
}