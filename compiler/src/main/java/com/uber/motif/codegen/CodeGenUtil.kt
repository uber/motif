package com.uber.motif.compiler.codegen

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.uber.motif.compiler.model.Dependency
import com.uber.motif.compiler.model.ObjectsClass
import javax.lang.model.element.TypeElement

val TypeElement.className: ClassName
    get() = ClassName.get(this)

val ObjectsClass.className: ClassName
    get() = type.className

val Dependency.className: ClassName
    get() = element.className

val Dependency.qualifierSpec: AnnotationSpec?
    get() = qualifier?.let { AnnotationSpec.get(qualifier) }