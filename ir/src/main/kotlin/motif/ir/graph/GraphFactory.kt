package motif.ir.graph

import motif.cache.ExtCache
import motif.cache.ExtCacheScope
import motif.ir.source.dependencies.Dependencies
import motif.ir.source.ScopeClass
import motif.ir.source.SourceSet
import motif.ir.source.base.Type
import motif.ir.source.child.ChildDeclaration
import motif.ir.source.dependencies.AnnotatedDependency
import motif.ir.source.dependencies.ExplicitDependencies

class GraphFactory private constructor(private val sourceSet: SourceSet) : ExtCache {

    override val cacheScope: ExtCacheScope = ExtCacheScope()

    private val scopeClasses: Map<Type, ScopeClass> = sourceSet.scopeClasses.associateBy { it.type }
    private val scopeDependencies: MutableMap<Type, Dependencies> = mutableMapOf()

    private var missingDependencies: Dependencies = Dependencies(listOf())

    private fun create(): Graph {
        val scopes = sourceSet.scopeClasses.map {
            val dependencies = it.type.dependencies()
            Scope(it, it.childDependencies, dependencies)
        }
        return Graph(missingDependencies, scopes, scopeDependencies)
    }

    private val Type.scopeClass: ScopeClass?
        get() = scopeClasses[this]

    private fun Type.dependencies(): Dependencies {
        return scopeDependencies.computeIfAbsent(this) {
            generatedDependencies?.let { return@computeIfAbsent it }
            val scopeClass = scopeClass ?: throw IllegalStateException("ScopeClass not found for: $this")
            scopeClass.validatedDependencies
        }
    }

    private val Type.generatedDependencies: Dependencies? by cache {
        sourceSet.generatedDependencies[this]
    }

    private val ScopeClass.dependencies: Dependencies by cache {
        childDependencies - exposed + selfDependencies
    }

    private val ScopeClass.childDependencies: Dependencies by cache {
        childDeclarations.map { it.calculateDependencies() }.map { it.toTransitive() }.merge()
    }

    private fun List<Dependencies>.merge(): Dependencies {
        return when {
            isEmpty() -> Dependencies(listOf())
            size == 1 -> this[0]
            else -> reduce { acc, dependencies -> acc + dependencies }
        }
    }

    private fun ChildDeclaration.calculateDependencies(): Dependencies {
        val childDependencies = method.scope.dependencies()
        val dynamicDependencies = method.dynamicDependencies
        return childDependencies.filter {
            // Only allow dynamic dependencies to satisfy non-transitive dependencies.
            // TODO If transitive, remove satisfied non-transitive scopes from AnnotatedDependency.consumingScopes
            it.transitive || it.dependency !in dynamicDependencies
        }
    }

    private val ScopeClass.validatedDependencies: Dependencies by cache {
        explicitDependencies?.let { explicitDependencies ->
            val missingDependencies = dependencies - explicitDependencies.dependencies
            this@GraphFactory.missingDependencies += missingDependencies
            return@cache explicitDependencies.override(this, dependencies)
        }
        dependencies
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