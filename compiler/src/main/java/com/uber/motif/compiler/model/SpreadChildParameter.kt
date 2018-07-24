package com.uber.motif.compiler.model

import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.DeclaredType

class SpreadChildParameter(
        override val element: VariableElement,
        override val type: DeclaredType,
        val methods: Map<Dependency, ExecutableElement>) : ChildMethodParameter {

    override val dependencies: List<Dependency> by lazy {
        methods.keys.toList()
    }

    companion object {

        fun fromParameter(
                env: ProcessingEnvironment,
                parameter: VariableElement,
                parameterType: DeclaredType): SpreadChildParameter {
            val methods = SpreadUtil.spreadMethods(env, parameterType)
            return SpreadChildParameter(parameter, parameterType, methods)
        }
    }
}