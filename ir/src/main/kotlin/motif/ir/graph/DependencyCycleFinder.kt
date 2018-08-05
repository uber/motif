package motif.ir.graph

import motif.ir.source.ScopeClass
import motif.ir.source.base.Dependency
import motif.ir.source.objects.FactoryMethod

class DependencyCycleFinder(private val scopeClass: ScopeClass) {

    private val factoryMethods: Map<Dependency, FactoryMethod> = scopeClass.objectsClass
            ?.factoryMethods
            ?.associateBy { it.providedDependency }
            ?: mapOf()

    fun findCycle(): DependencyCycle? {
        factoryMethods.values.forEach {
            findCycle(listOf(), it)?.let { return it }
        }
        return null
    }

    private fun findCycle(visited: List<FactoryMethod>, factoryMethod: FactoryMethod): DependencyCycle? {
        if (factoryMethod in visited) {
            return DependencyCycle(scopeClass, visited)
        }

        val newVisited = visited + factoryMethod

        factoryMethod.consumedDependencies
                .mapNotNull { factoryMethods[it] }
                .forEach { findCycle(newVisited, it)?.let { return it } }

        return null
    }
}