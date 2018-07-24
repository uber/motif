package com.uber.motif.compiler.model

import com.uber.motif.compiler.methods
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ExecutableType
import javax.lang.model.type.TypeKind

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
            val methods = parameterType.methods(env)
                    // Only consume non-void parameterless methods.
                    .filter { it.returnType.kind != TypeKind.VOID && it.parameters.isEmpty() }
                    .associateBy { method ->
                        val methodType = env.typeUtils.asMemberOf(parameterType, method) as ExecutableType
                        Dependency.providedByReturn(parameterType, method, methodType)
                    }
            return SpreadChildParameter(parameter, parameterType, methods)
        }
    }
}