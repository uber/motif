package motif.ir.graph

import motif.ir.graph.errors.MissingDependenciesError
import motif.ir.graph.errors.ScopeCycleError
import motif.ir.graph.errors.UnprocessedScopeError

class GraphErrors(
        val scopeCycleError: ScopeCycleError?,
        val unprocessedScopeError: UnprocessedScopeError?,
        val missingDependenciesError: MissingDependenciesError?) {

    val isEmpty: Boolean = scopeCycleError == null
            && unprocessedScopeError == null
            && missingDependenciesError == null

    fun getMessage(): String {
        return "TODO: Implement graph error message."
    }
}