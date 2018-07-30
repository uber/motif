package com.uber.motif.compiler.errors

import com.uber.motif.compiler.CompilationError
import com.uber.motif.compiler.model.Dependency
import javax.lang.model.type.DeclaredType

class MissingDependencies(val scopeType: DeclaredType, val missingDependencies: Set<Dependency>) : CompilationError() {

    override val message = "Scope $scopeType is missing dependencies: $missingDependencies"
}