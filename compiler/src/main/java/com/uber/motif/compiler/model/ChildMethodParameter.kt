package com.uber.motif.compiler.model

import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror

interface ChildMethodParameter {
    val element: VariableElement
    val type: TypeMirror
    val dependencies: List<Dependency>
}