package com.uber.motif.compiler.model

import javax.lang.model.element.ExecutableElement

class BasicProviderMethod(
        override val method: ExecutableElement,
        override val providedDependency: Dependency,
        override val requiredDependencies: List<Dependency>) : ProviderMethod {

    companion object {

        fun fromMethod(method: ExecutableElement): BasicProviderMethod {
            val providedDependency = Dependency.providedByReturn(method)
            val requiredDependencies = Dependency.requiredByParams(method)
            return BasicProviderMethod(method, providedDependency, requiredDependencies)
        }
    }
}