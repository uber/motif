package com.uber.motif.compiler.model

import javax.lang.model.element.ExecutableElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ExecutableType

class ExposerMethod(val element: ExecutableElement, val dependency: Dependency) {

    companion object {

        fun fromMethod(
                owner: DeclaredType,
                method: ExecutableElement,
                methodType: ExecutableType): ExposerMethod {
            if (method.parameters.isNotEmpty()) {
                throw RuntimeException("Exposer method cannot take any parameters: $method")
            }
            val dependency = Dependency.requiredByReturn(owner, method, methodType)
            return ExposerMethod(method, dependency)
        }
    }
}