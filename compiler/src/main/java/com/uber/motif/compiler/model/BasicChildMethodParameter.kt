package com.uber.motif.compiler.model

import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror

class BasicChildMethodParameter(
        override val element: VariableElement,
        override val type: TypeMirror,
        val dependency: Dependency) : ChildMethodParameter {

    override val dependencies: List<Dependency> = listOf(dependency)
}