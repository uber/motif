package com.uber.motif.compiler.model

import javax.lang.model.element.ExecutableElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ExecutableType

class ChildMethod(
        val method: ExecutableElement,
        val methodType: ExecutableType,
        val scopeType: DeclaredType,
        val dynamicDependencies: List<Dependency>) {

    companion object {

        fun fromMethod(
                owner: DeclaredType,
                method: ExecutableElement,
                methodType: ExecutableType): ChildMethod {
            val scopeType = methodType.returnType as DeclaredType
            val dynamicDependencies = Dependency.requiredByParams(owner, method, methodType)
            return ChildMethod(method, methodType, scopeType, dynamicDependencies)
        }
    }
}