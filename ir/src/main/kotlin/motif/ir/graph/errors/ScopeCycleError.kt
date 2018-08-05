package motif.ir.graph.errors

import motif.ir.source.base.Type

class ScopeCycleError(val cycle: List<Type>) : RuntimeException()