package motif.ir.graph

import motif.ir.source.ScopeClass

class DependencyCycleFinder(private val scopeClass: ScopeClass) {

    fun findCycle(): DependencyCycle? {
        val objectsClass = scopeClass.objectsClass ?: return null

        // TODO
        return null
    }
}