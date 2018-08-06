package motif.ir.graph.errors

import motif.ir.source.base.Type

data class UnprocessedScopeError(val scopeType: Type) : RuntimeException()