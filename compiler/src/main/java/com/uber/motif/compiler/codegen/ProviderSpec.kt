package com.uber.motif.compiler.codegen

import com.squareup.javapoet.*
import com.uber.motif.compiler.graph.ResolvedScope
import com.uber.motif.compiler.isPublic
import com.uber.motif.compiler.model.ProviderMethod
import dagger.Provides

abstract class ProviderSpec(
        protected val resolvedScope: ResolvedScope,
        protected val daggerScope: DaggerScopeSpec,
        protected val internalQualifier: InternalQualifierSpec,
        protected val providerMethod: ProviderMethod,
        protected val name: String) {

    private val dependencyName = providerMethod.providedDependency.className

    val parameters: List<DependencyParameterSpec> = DependencyParameterSpec.fromDependencies(
            resolvedScope,
            internalQualifier,
            providerMethod.requiredDependencies)

    // Lazy initialization so implementations of abstract returnStatement method can access their own values safely.
    val spec: MethodSpec by lazy {
        val parameterSpecs: List<ParameterSpec> = parameters.map { it.spec }
        MethodSpec.methodBuilder(name).apply {
            val isInternal = !providerMethod.method.isPublic
            internalQualifier.annotationSpec(providerMethod.providedDependency, isInternal)?.let { addAnnotation(it) }
            addAnnotation(Provides::class.java)
            if (providerMethod.cache) {
                addAnnotation(daggerScope.className)
            }
            addParameters(parameterSpecs)
            returns(dependencyName)
            val callParams: String = parameters.joinToString(", ") { "\$N" }
            addStatement(returnStatement(dependencyName, callParams, parameterSpecs.toTypedArray()))
        }.build()
    }

    abstract fun returnStatement(
            dependencyName: TypeName,
            callParams: String,
            parameters: Array<ParameterSpec>): CodeBlock
}