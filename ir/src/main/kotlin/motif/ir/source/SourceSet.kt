package motif.ir.source

import motif.ir.source.base.Type
import motif.ir.source.dependencies.Dependencies

class SourceSet(val scopeClasses: List<ScopeClass>) {

    val generatedDependencies: Map<Type, Dependencies> by lazy {
        scopeClasses
                .flatMap { it.childDeclarations }
                .filter { it.generatedDependencies != null }
                .associateBy({ it.method.scope }) {
                    Dependencies(it.generatedDependencies!!.annotatedDependencies)
                }
    }
}