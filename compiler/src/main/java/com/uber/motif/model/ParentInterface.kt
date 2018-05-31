package com.uber.motif.compiler.model

import com.uber.motif.compiler.methods
import javax.lang.model.element.TypeElement

class ParentInterface(
        val type: TypeElement,
        val methods: List<ParentInterfaceMethod>) {

    val dependencies: Set<Dependency> by lazy {
        methods.map { it.dependency }.toSet()
    }

    companion object {

        fun create(type: TypeElement): ParentInterface {
            val methods = type.methods().map { ParentInterfaceMethod.fromMethod(it) }
            return ParentInterface(type, methods)
        }
    }
}