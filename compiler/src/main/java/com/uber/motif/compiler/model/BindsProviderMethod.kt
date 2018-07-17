package com.uber.motif.compiler.model

import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ExecutableElement

class BindsProviderMethod(
        override val method: ExecutableElement,
        override val providedDependency: Dependency,
        val requiredDependency: Dependency) : ProviderMethod {

    override val requiredDependencies = listOf(requiredDependency)

    companion object {

        fun fromMethod(env: ProcessingEnvironment, method: ExecutableElement): BindsProviderMethod {
            val providedDependency = Dependency.providedByReturn(method)
            val requiredDependency = Dependency.requiredByParams(method)[0]
            if (!env.typeUtils.isAssignable(requiredDependency.element.asType(), providedDependency.element.asType())) {
                throw RuntimeException("Type ${requiredDependency.element} bound by $method is not assignable to ${providedDependency.element}")
            }
            return BindsProviderMethod(method, providedDependency, requiredDependency)
        }
    }
}