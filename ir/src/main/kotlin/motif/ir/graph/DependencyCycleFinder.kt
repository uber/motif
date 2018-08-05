package motif.ir.graph

import motif.ir.source.ScopeClass
import motif.ir.source.base.Dependency

class DependencyCycleFinder(private val scopeClass: ScopeClass) {

    fun findCycle(): List<Dependency>? {
        val objectsClass = scopeClass.objectsClass ?: return null

        // TODO
        return null
    }
}