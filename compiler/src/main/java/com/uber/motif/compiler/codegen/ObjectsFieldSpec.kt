package com.uber.motif.compiler.codegen

import com.squareup.javapoet.FieldSpec
import javax.lang.model.element.Modifier

class ObjectsFieldSpec(objectsImplSpec: ObjectsImplSpec) {

    val spec: FieldSpec? = objectsImplSpec.spec?.let {
        FieldSpec.builder(objectsImplSpec.className, "objects", Modifier.PRIVATE, Modifier.FINAL)
                .initializer("new \$T()", objectsImplSpec.className)
                .build()
    }
}