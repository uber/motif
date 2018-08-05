package motif.ir.graph

import motif.ir.source.ScopeClass
import motif.ir.source.SourceSet
import motif.ir.source.base.Type
import motif.ir.source.dependencies.AnnotatedDependency
import motif.ir.source.dependencies.Dependencies
import motif.ir.source.dependencies.ExplicitDependencies

class GraphFactory private constructor(private val sourceSet: SourceSet) {

    private val scopeClasses: Map<Type, ScopeClass> = sourceSet.scopeClasses.associateBy { it.type }
    private val childDependencies: MutableMap<Type, Dependencies> = mutableMapOf()
    private val scopeDependencies: MutableMap<Type, Dependencies> = mutableMapOf()

    private var missingDependencies: Dependencies = Dependencies(listOf())

    private fun create(): Graph {
        val scopes = sourceSet.scopeClasses.map {
            val dependencies = it.type.dependencies()
            Scope(it, it.childDependencies(), dependencies)
        }
        return Graph(
                missingDependencies,
                scopes,
                scopeDependencies)
    }

    private val Type.scopeClass: ScopeClass?
        get() = scopeClasses[this]

    private fun Type.dependencies(): Dependencies {
        return scopeDependencies.computeIfAbsent(this) {
            sourceSet.generatedDependencies[this]?.let { return@computeIfAbsent it }
            val scopeClass = scopeClass ?: throw IllegalStateException("ScopeClass not found for: $this")
            val dependencies = scopeClass.childDependencies() - scopeClass.exposed + scopeClass.selfDependencies
            scopeClass.explicitDependencies?.let { explicitDependencies ->
                val missingDependencies = dependencies - explicitDependencies.dependencies
                this@GraphFactory.missingDependencies += missingDependencies
                return@computeIfAbsent explicitDependencies.override(scopeClass, dependencies)
            }
            dependencies
        }
    }

    private fun ScopeClass.childDependencies(): Dependencies {
        return childDependencies.computeIfAbsent(type) {
            childDeclarations
                    .map {
                        val childDependencies = it.method.scope.dependencies()
                        val dynamicDependencies = it.method.dynamicDependencies
                        childDependencies.filter {
                            // Only allow dynamic dependencies to satisfy non-transitive dependencies.
                            // TODO If transitive, remove satisfied non-transitive scopes from AnnotatedDependency.consumingScopes
                            it.transitive || it.dependency !in dynamicDependencies
                        }
                    }
                    .map { it.toTransitive() }
                    .merge()

        }
    }

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

    companion object {

        fun create(sourceSet: SourceSet): Graph {
            return GraphFactory(sourceSet).create()
        }
    }
}