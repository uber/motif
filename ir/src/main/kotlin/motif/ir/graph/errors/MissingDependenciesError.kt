package motif.ir.graph.errors

import motif.ir.source.dependencies.Dependencies

data class MissingDependenciesError(val dependencies: Dependencies)