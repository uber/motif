package motif.ir.graph.errors

data class GraphErrors(
        val scopeCycleError: ScopeCycleError?,
        val unprocessedScopeError: UnprocessedScopeError?,
        val missingDependenciesError: MissingDependenciesError?,
        val dependencyCycleError: DependencyCycleError?,
        val duplicateFactorMethodsError: DuplicateFactorMethodsError?) {

    val isEmpty: Boolean = scopeCycleError == null
            && unprocessedScopeError == null
            && missingDependenciesError == null
            && dependencyCycleError == null
            && duplicateFactorMethodsError == null

    fun getMessage(): String {
        return "$this"
    }
}