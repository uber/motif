package motif.ir.graph.errors

class GraphErrors(
        val scopeCycleError: ScopeCycleError?,
        val unprocessedScopeError: UnprocessedScopeError?,
        val missingDependenciesError: MissingDependenciesError?,
        val dependencyCycleError: DependencyCycleError?) {

    val isEmpty: Boolean = scopeCycleError == null
            && unprocessedScopeError == null
            && missingDependenciesError == null
            && dependencyCycleError == null

    fun getMessage(): String {
        return "TODO: Implement graph error message."
    }
}