package motif.ir.graph.errors

import motif.ir.source.base.Type

class UnprocessedScopeError(val scopeType: Type) : RuntimeException(), GraphError {

    override val message: String = StringBuilder().apply {
        appendln()
        appendln("===== UNPROCESSED SCOPE FOUND ====")
    }.toString()
}