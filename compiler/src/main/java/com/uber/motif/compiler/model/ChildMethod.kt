package com.uber.motif.compiler.model

import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ExecutableElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ExecutableType

class ChildMethod(
        override val env: ProcessingEnvironment, // Hack until https://github.com/square/javapoet/issues/656 is resolved
        override val owner: DeclaredType,
        override val method: ExecutableElement,
        override val methodType: ExecutableType,
        val scopeType: DeclaredType,
        val dynamicDependencies: List<Dependency>) : Method {

    companion object {

        fun fromMethod(
                env: ProcessingEnvironment,
                owner: DeclaredType,
                method: ExecutableElement,
                methodType: ExecutableType): ChildMethod {
            val scopeType = methodType.returnType as DeclaredType
            val dynamicDependencies = Dependency.requiredByParams(owner, method, methodType)
            return ChildMethod(env, owner, method, methodType, scopeType, dynamicDependencies)
        }
    }
}