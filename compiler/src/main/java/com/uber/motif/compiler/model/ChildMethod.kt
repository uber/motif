package com.uber.motif.compiler.model

import com.uber.motif.Spread
import com.uber.motif.compiler.hasAnnotation
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
        val parameters: List<ChildMethodParameter>) : Method {

    val dynamicDependencies: List<Dependency> by lazy {
        parameters.flatMap { it.dependencies }
    }

    companion object {

        fun fromMethod(
                env: ProcessingEnvironment,
                owner: DeclaredType,
                method: ExecutableElement,
                methodType: ExecutableType): ChildMethod {
            val scopeType = methodType.returnType as DeclaredType
            val parameters = parameters(env, owner, method, methodType)
            return ChildMethod(env, owner, method, methodType, scopeType, parameters)
        }

        private fun parameters(
                env: ProcessingEnvironment,
                owner: DeclaredType,
                method: ExecutableElement,
                methodType: ExecutableType): List<ChildMethodParameter> {
            // TODO throw error for multiple parameters providing the same dependency.
            val parameterTypes = methodType.parameterTypes
            return method.parameters.mapIndexed { i, parameter ->
                val parameterType = parameterTypes[i]
                if (parameter.hasAnnotation(Spread::class)) {
                    SpreadChildParameter.fromParameter(env, parameter, parameterType as DeclaredType)
                } else {
                    val dependency = Dependency.fromParam(owner, method, parameter, parameterType, true)
                    BasicChildMethodParameter(parameter, parameterType, dependency)
                }
            }
        }
    }
}