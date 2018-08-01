package motif.ir.source.dependencies

import motif.ir.source.base.Dependency
import motif.ir.source.base.Type

class AnnotatedDependency(
        val dependency: Dependency,
        val transitive: Boolean,
        val consumingScopes: Set<Type>) {

    override fun toString(): String {
        return "$dependency $consumingScopes"
    }
}