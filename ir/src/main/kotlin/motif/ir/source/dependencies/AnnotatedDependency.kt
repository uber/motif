package motif.ir.source.dependencies

import motif.ir.source.base.Dependency
import motif.ir.source.base.Type

class AnnotatedDependency(
        val dependency: Dependency,
        val transitive: Boolean,
        val consumingScopes: Set<Type>) {

    operator fun minus(scope: Type): AnnotatedDependency {
        return AnnotatedDependency(dependency, transitive, consumingScopes - scope)
    }

    override fun toString(): String {
        return "$dependency $consumingScopes"
    }
}