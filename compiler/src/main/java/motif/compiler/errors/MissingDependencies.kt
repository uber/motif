package motif.compiler.errors

import motif.compiler.CompilationError
import motif.compiler.model.Dependency
import javax.lang.model.type.DeclaredType

class MissingDependencies(val scopeType: DeclaredType, val missingDependencies: Set<Dependency>) : CompilationError() {

    override val message = "Scope $scopeType is missing dependencies: $missingDependencies"
}