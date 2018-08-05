package motif.ir.graph

import motif.ir.graph.errors.ScopeCycleError
import motif.ir.graph.errors.UnprocessedScopeError
import motif.ir.source.dependencies.Dependencies

class GraphErrors(
        val scopeCycleError: ScopeCycleError?,
        val unprocessedScopeError: UnprocessedScopeError?,
        val missingDependencies: Dependencies?) {

    val isEmpty: Boolean = scopeCycleError == null
            && unprocessedScopeError == null
            && missingDependencies == null

    fun getMessage(): String {
        return "TODO: Implement graph error message.: $isEmpty"
    }
}