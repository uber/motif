package com.uber.motif.compiler.codegen

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeSpec
import com.uber.motif.compiler.graph.ResolvedScope
import com.uber.motif.compiler.names.Names

class ObjectsImplSpec(
        resolvedScope: ResolvedScope) {

    val className: ClassName = resolvedScope.scopeImplName.nestedClass(Names.OBJECTS_CLASS_NAME)
    val spec: TypeSpec? = resolvedScope.scope.objectsClass?.let { objectsClass ->
        TypeSpec.classBuilder(className).apply {
            superclass(objectsClass.className)
            objectsClass.abstractProviderMethods.map {
                it.override()
                        .addStatement("throw new \$T()", UnsupportedOperationException::class.java)
                        .build()
            }.forEach { addMethod(it) }
        }.build()
    }
}