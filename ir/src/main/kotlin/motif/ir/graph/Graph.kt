package motif.ir.graph

import motif.ir.graph.errors.ScopeCycleError
import motif.ir.graph.errors.UnprocessedScopeError
import motif.ir.source.ScopeClass
import motif.ir.source.base.Type
import motif.ir.source.dependencies.Dependencies

class Graph(
        private val allNodes: Map<Type, Node>,
        private val nodes: Map<ScopeClass, Node>,
        val scopeCycleError: ScopeCycleError?,
        val unprocessedScopeError: UnprocessedScopeError?) {

    val scopes: List<Scope> = nodes.map { (scopeClass, node) ->
        Scope(scopeClass, node.childDependencies, node.dependencies)
    }

    val missingDependencies: Dependencies? by lazy {
        val allMissingDepenendencies: List<Dependencies> = nodes.values.mapNotNull { it.missingDependencies }
        if (allMissingDepenendencies.isEmpty()) {
            null
        }  else {
            allMissingDepenendencies.reduce { acc, dependencies -> acc + dependencies }
        }
    }

    fun getDependencies(scopeType: Type): Dependencies? {
        return allNodes[scopeType]?.dependencies
    }
}