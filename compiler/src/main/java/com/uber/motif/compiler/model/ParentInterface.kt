package com.uber.motif.compiler.model

import com.uber.motif.compiler.methodType
import com.uber.motif.compiler.methods
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ExecutableType

class ParentInterface(
        val type: DeclaredType,
        val methods: List<ParentInterfaceMethod>) {

    val dependencies: Set<Dependency> by lazy {
        methods.map { it.dependency }.toSet()
    }

    companion object {

        fun create(env: ProcessingEnvironment, type: DeclaredType): ParentInterface {
            val methods = type.methods(env).map { method ->
                val methodType: ExecutableType = type.methodType(env, method)
                ParentInterfaceMethod.fromMethod(type, method, methodType)
            }
            return ParentInterface(type, methods)
        }
    }
}