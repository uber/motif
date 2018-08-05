package motif.ir.graph

import motif.ir.source.ScopeClass
import motif.ir.source.dependencies.AnnotatedDependency
import motif.ir.source.dependencies.Dependencies
import motif.ir.source.dependencies.ExplicitDependencies

class ScopeClassNode(
        private val scopeClass: ScopeClass,
        private val scopeChildren: List<ScopeChild>) : Node {

    private var internalMissingDependencies: Dependencies? = null

    override val missingDependencies: Dependencies? by lazy {
        dependencies // Ensure dependencies are resolved.
        internalMissingDependencies
    }

    override val dependencyCycle: DependencyCycle? by lazy {
        DependencyCycleFinder(scopeClass).findCycle()
    }

    override val childDependencies: Dependencies by lazy {
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

    override val dependencies: Dependencies by lazy {
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

    override val children: List<Node> = scopeChildren.map { it.node }

    override val parents: MutableList<Node> = mutableListOf()

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