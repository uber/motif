package motif.ir.graph

import motif.ir.source.base.Type
import motif.ir.source.dependencies.Dependencies

class Graph(
        val missingDependencies: Dependencies,
        val scopes: List<Scope>,
        val scopeDependencies: Map<Type, Dependencies>) {

    private val map: Map<Type, Scope> = scopes.associateBy { it.scopeClass.type }

    fun getScope(type: Type): Scope? {
        return map[type]
    }

    fun getDependencies(type: Type): Dependencies? {
        return scopeDependencies[type]
    }
}