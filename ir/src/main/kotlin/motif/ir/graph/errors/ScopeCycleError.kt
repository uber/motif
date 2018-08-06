package motif.ir.graph.errors

import motif.ir.source.base.Type

data class ScopeCycleError(val cycle: List<Type>) : RuntimeException()