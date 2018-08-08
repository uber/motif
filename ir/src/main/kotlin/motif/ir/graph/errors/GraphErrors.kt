package motif.ir.graph.errors

class GraphErrors(
        val scopeCycleError: ScopeCycleError?,
        val unprocessedScopeError: UnprocessedScopeError?,
        val missingDependenciesError: MissingDependenciesError?,
        val dependencyCycleError: DependencyCycleError?,
        val duplicateFactoryMethodsError: DuplicateFactoryMethodsError?)
    : List<GraphError> by listOfNotNull(
        scopeCycleError,
        unprocessedScopeError,
        missingDependenciesError,
        dependencyCycleError,
        duplicateFactoryMethodsError)