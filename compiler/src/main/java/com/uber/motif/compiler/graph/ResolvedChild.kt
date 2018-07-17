package com.uber.motif.compiler.graph

import com.squareup.javapoet.ClassName
import com.uber.motif.compiler.codegen.className
import com.uber.motif.compiler.model.ChildMethod
import com.uber.motif.compiler.model.Dependency

class ResolvedChild(
        val method: ChildMethod,
        val resolvedParent: ResolvedParent) {

    val externalDependencies: List<Dependency> = resolvedParent.methods.map { it.dependency } - method.dynamicDependencies
    val scopeName: ClassName = method.scopeType.className
    val scopeImplName: ClassName = ClassNames.scopeImpl(method.scopeType)
    val baseName: String = method.scopeType.simpleName.toString().removeSuffix("Scope")
}