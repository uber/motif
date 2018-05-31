package com.uber.motif.compiler.model

import javax.lang.model.element.ExecutableElement

class ParentInterfaceMethod(
        val name: String,
        val dependency: Dependency) {

    companion object {

        fun fromMethod(method: ExecutableElement): ParentInterfaceMethod {
            if (method.parameters.size > 0) throw RuntimeException("Parent interface method must not take any parameters: $method")
            val dependency = Dependency.fromReturnType(method)
            val methodName = method.simpleName.toString()
            return ParentInterfaceMethod(methodName, dependency)
        }
    }
}