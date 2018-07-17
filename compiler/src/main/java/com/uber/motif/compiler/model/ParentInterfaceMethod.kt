package com.uber.motif.compiler.model

import com.uber.motif.compiler.hasAnnotation
import com.uber.motif.internal.Transitive
import javax.lang.model.element.ExecutableElement

class ParentInterfaceMethod(
        val name: String,
        val isTransitive: Boolean,
        val dependency: Dependency) {

    companion object {

        fun fromMethod(method: ExecutableElement): ParentInterfaceMethod {
            if (method.parameters.size > 0) throw RuntimeException("Parent interface method must not take any parameters: $method")
            val dependency = Dependency.requiredByReturn(method)
            val methodName = method.simpleName.toString()
            val transitive = method.hasAnnotation(Transitive::class)
            return ParentInterfaceMethod(methodName, transitive, dependency)
        }
    }
}