package com.uber.motif.compiler.codegen

import com.squareup.javapoet.ParameterSpec
import com.uber.motif.compiler.graph.ResolvedScope
import com.uber.motif.compiler.model.Dependency
import com.uber.motif.compiler.names.UniqueNameSet

class DependencyParameterSpec(
        resolvedScope: ResolvedScope,
        internalQualifier: InternalQualifierSpec,
        dependency: Dependency,
        name: String) {

    val spec: ParameterSpec = ParameterSpec.builder(dependency.className, name).apply {
        val isInternal = dependency in resolvedScope.scope.providedPrivateDependencies
        internalQualifier.annotationSpec(dependency, isInternal)?.let { addAnnotation(it) }
    }.build()

    companion object {

        fun fromDependencies(
                resolvedScope: ResolvedScope,
                internalQualifier: InternalQualifierSpec,
                dependencies: List<Dependency>): List<DependencyParameterSpec> {
            val parameterNames = UniqueNameSet()
            return dependencies.map { dependency ->
                DependencyParameterSpec(
                        resolvedScope,
                        internalQualifier,
                        dependency,
                        parameterNames.unique(dependency.preferredName))
            }
        }
    }
}