package motif.ir.graph

import motif.ir.source.ScopeClass
import motif.ir.source.base.Dependency
import motif.ir.source.dependencies.AnnotatedDependency
import motif.ir.source.dependencies.Dependencies
import motif.ir.source.dependencies.ExplicitDependencies
import motif.ir.source.objects.FactoryMethod

class Node(
        val scopeClass: ScopeClass,
        private val scopeChildren: List<ScopeChild>) {

    private var internalMissingDependencies: Dependencies? = null

    val missingDependencies: Dependencies? by lazy {
        dependencies // Ensure dependencies are resolved.
        internalMissingDependencies
    }

    val dependencyCycle: DependencyCycle? by lazy {
        DependencyCycleFinder(scopeClass).findCycle()
    }

    val childDependencies: Dependencies by lazy {
        scopeChildren
                .map { child ->
                    val childDependencies: Dependencies = child.node.dependencies
                    val dynamicDependencies = child.method.dynamicDependencies
                    childDependencies.filter {
                        // Only allow dynamic dependencies to satisfy non-transitive dependencies.
                        // TODO If transitive, remove satisfied non-transitive scopes from AnnotatedDependency.consumingScopes
                        it.transitive || it.dependency !in dynamicDependencies
                    }
                }
                .map { it.toTransitive() }
                .merge()
    }

    val dependencies: Dependencies by lazy {
        val dependencies = childDependencies - scopeClass.exposed + scopeClass.selfDependencies
        scopeClass.explicitDependencies?.let { explicitDependencies ->
            val missingDependencies = dependencies - explicitDependencies.dependencies
            if (missingDependencies.list.isNotEmpty()) {
                internalMissingDependencies = missingDependencies
            }
            return@lazy explicitDependencies.override(scopeClass, dependencies)
        }
        dependencies
    }

    private val ancestorFactoryMethods: List<FactoryMethod> by lazy {
        parents.flatMap { it.exposedFactoryMethods }
    }

    private val exposedFactoryMethods: List<FactoryMethod> by lazy {
        scopeClass.factoryMethods.filter { it.isExposed } + ancestorFactoryMethods
    }

    val duplicateFactoryMethods: List<DuplicateFactoryMethod> by lazy {
        val visibleFactoryMethods: Map<Dependency, List<FactoryMethod>> = (scopeClass.factoryMethods + ancestorFactoryMethods)
                .groupBy { it.providedDependency }

        scopeClass.factoryMethods.mapNotNull { factoryMethod ->
            val visibleFactoryMethodList = visibleFactoryMethods[factoryMethod.providedDependency] ?: throw IllegalStateException()
            if (visibleFactoryMethodList.size > 1) {
                DuplicateFactoryMethod(factoryMethod, visibleFactoryMethodList - factoryMethod)
            } else {
                null
            }
        }
    }

    val children: List<Node> = scopeChildren.map { it.node }

    val parents: MutableList<Node> = mutableListOf()

    private fun List<Dependencies>.merge(): Dependencies {
        return when {
            isEmpty() -> Dependencies(listOf())
            size == 1 -> this[0]
            else -> reduce { acc, dependencies -> acc + dependencies }
        }
    }

    private fun ExplicitDependencies.override(scopeClass: ScopeClass, dependencies: Dependencies): Dependencies {
        val list = this.dependencies.map {
            dependencies[it] ?: AnnotatedDependency(it, false, setOf(scopeClass.type))
        }
        return Dependencies(list)
    }
}