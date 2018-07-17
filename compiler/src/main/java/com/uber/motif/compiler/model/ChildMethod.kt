package com.uber.motif.compiler.model

import com.uber.motif.compiler.asTypeElement
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

class ChildMethod(
        val method: ExecutableElement,
        val scopeType: TypeElement,
        val dynamicDependencies: List<Dependency>) {

    companion object {

        fun fromMethod(method: ExecutableElement): ChildMethod {
            val scopeType = method.returnType.asTypeElement()
            val dynamicDependencies = Dependency.requiredByParams(method)
            return ChildMethod(method, scopeType, dynamicDependencies)
        }
    }
}