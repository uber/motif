package com.uber.motif.compiler.graph

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import com.uber.motif.compiler.codegen.className
import com.uber.motif.compiler.model.*

/**
 * After recursively calculating the dependencies required from the parent of a scope, we consider the scope resolved.
 * Scope resolution happens in ResolvedGraph where we also validate that we don't have any missing dependencies in cases
 * where the developer has explicitly defined what he/she expects to be provided by the parent.
 */
class ResolvedScope(
        val scope: ScopeClass,
        val parent: ResolvedParent,
        val children: List<ResolvedChild>) {
    val scopeName: ClassName = scope.type.className
    val scopeImplName: ClassName = ClassNames.scopeImpl(scope.type)
    val packageName: String = scopeImplName.packageName()
}
