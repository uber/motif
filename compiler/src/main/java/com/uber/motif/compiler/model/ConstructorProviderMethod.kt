package com.uber.motif.compiler.model

import com.uber.motif.compiler.constructors
import javax.lang.model.element.ExecutableElement

class ConstructorProviderMethod(
        val constructor: ExecutableElement,
        override val method: ExecutableElement,
        override val providedDependency: Dependency,
        override val requiredDependencies: List<Dependency>) : ProviderMethod {

    companion object {

        fun fromMethod(method: ExecutableElement): ConstructorProviderMethod {
            val providedDependency = Dependency.providedByReturn(method)
            val constructor = providedDependency.element.constructors().apply {
                // TODO Handle this better. Require @Inject if multiple constructor exist? Require @Inject always?
                if (size > 1) throw RuntimeException("Multiple constructors found for type $providedDependency provided by $method")
            }[0]

            val requiredDependencies = Dependency.requiredByParams(constructor)
            return ConstructorProviderMethod(constructor, method, providedDependency, requiredDependencies)
        }
    }
}