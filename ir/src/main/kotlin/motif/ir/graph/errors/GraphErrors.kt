package motif.ir.graph.errors

class GraphErrors(
        val scopeCycleError: ScopeCycleError?,
        val missingDependenciesError: MissingDependenciesError?,
        val dependencyCycleError: DependencyCycleError?,
        val duplicateFactoryMethodsError: DuplicateFactoryMethodsError?)
    : List<GraphError> by listOfNotNull(
        scopeCycleError,
        missingDependenciesError,
        dependencyCycleError,
        duplicateFactoryMethodsError)