package motif.ir.graph.errors

import motif.ir.graph.DependencyCycle

data class DependencyCycleError(val cycles: List<DependencyCycle>)