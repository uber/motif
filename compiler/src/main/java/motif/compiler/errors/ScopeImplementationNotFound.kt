package motif.compiler.errors

import motif.compiler.CompilationError
import javax.lang.model.type.DeclaredType

class ScopeImplementationNotFound(val scopeType: DeclaredType) : CompilationError() {

    override val message = "Scope implementation not found for $scopeType. This can happen if a child " +
            "Gradle module didn't the Motif annotation processor."
}