package motif.ir.graph.errors

import motif.ir.source.base.Type

class UnprocessedScopeError(val scopeType: Type) : RuntimeException()