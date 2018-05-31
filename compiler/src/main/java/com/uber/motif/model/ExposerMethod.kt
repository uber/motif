package com.uber.motif.compiler.model

import javax.lang.model.element.ExecutableElement

class ExposerMethod(val element: ExecutableElement, val dependency: Dependency) {

    companion object {

        fun fromMethod(method: ExecutableElement): ExposerMethod {
            if (method.parameters.isNotEmpty()) {
                throw RuntimeException("Exposer method cannot take any parameters: $method")
            }
            val dependency = Dependency.fromReturnType(method)
            return ExposerMethod(method, dependency)
        }
    }
}