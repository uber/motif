package motif.ir.graph

import motif.ir.graph.errors.ScopeCycleError
import motif.ir.graph.errors.UnprocessedScopeError
import motif.ir.source.ScopeClass
import motif.ir.source.base.Dependency
import motif.ir.source.base.Type
import motif.ir.source.dependencies.Dependencies

class Graph(
        private val allNodes: Map<Type, Node>,
        private val nodes: Map<ScopeClass, Node>,
        private val scopeCycleError: ScopeCycleError?,
        private val unprocessedScopeError: UnprocessedScopeError?) {

    val scopes: List<Scope> = nodes.map { (scopeClass, node) ->
        Scope(scopeClass, node.childDependencies, node.dependencies)
    }

    val graphErrors: GraphErrors by lazy {
        GraphErrors(scopeCycleError, unprocessedScopeError, missingDependencies)
    }

    private val missingDependencies: Dependencies? by lazy {
        val allMissingDepenendencies: List<Dependencies> = nodes.values.mapNotNull { it.missingDependencies }
        if (allMissingDepenendencies.isEmpty()) {
            null
        }  else {
            allMissingDepenendencies.reduce { acc, dependencies -> acc + dependencies }
        }
    }

    val dependencyCycles: Map<Type, List<Dependency>?> by lazy {
        nodes.entries.associateBy({ it.key.type }) { it.value.dependencyCycle }
    }

    fun getDependencies(scopeType: Type): Dependencies? {
        return allNodes[scopeType]?.dependencies
    }
}