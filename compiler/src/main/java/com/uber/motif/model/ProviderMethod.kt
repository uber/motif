package com.uber.motif.compiler.model

import javax.lang.model.element.ExecutableElement

interface ProviderMethod {
    val method: ExecutableElement
    val providedDependency: Dependency
    val requiredDependencies: List<Dependency>
}