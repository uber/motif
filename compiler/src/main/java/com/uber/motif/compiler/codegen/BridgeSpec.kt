package com.uber.motif.compiler.codegen

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeSpec
import com.uber.motif.compiler.graph.ResolvedChild
import com.uber.motif.compiler.graph.ResolvedScope
import javax.lang.model.element.Modifier

class BridgeSpec(
        resolvedScope: ResolvedScope,
        child: ResolvedChild,
        name: String) {

    val className: ClassName = resolvedScope.scopeImplName.nestedClass(name)
    val parent = BridgeParentSpec(child, className)
    val method = BridgeMethodSpec(child, parent)
    val spec: TypeSpec? = TypeSpec.classBuilder(className).apply {
        addModifiers(Modifier.PRIVATE, Modifier.STATIC)
        addMethod(method.spec)
        addType(parent.spec)
    }.build()
}