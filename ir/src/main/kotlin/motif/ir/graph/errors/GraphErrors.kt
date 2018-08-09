package motif.ir.graph.errors

class GraphErrors(
        val scopeCycleError: ScopeCycleError?,
        val missingDependenciesErrors: List<MissingDependenciesError>,
        val dependencyCycleError: DependencyCycleError?,
        val duplicateFactoryMethodsError: DuplicateFactoryMethodsError?)
    : List<GraphError> by listOfNotNull(
        scopeCycleError,
        dependencyCycleError,
        duplicateFactoryMethodsError) + missingDependenciesErrors