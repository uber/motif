package motif.ir.graph.errors

import motif.ir.graph.DependencyCycle

class DependencyCycleError(val cycles: List<DependencyCycle>)