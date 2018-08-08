package motif.ir.graph.errors

import motif.ir.source.base.Type

class ScopeCycleError(val cycle: List<Type>) : RuntimeException(), GraphError {

    override val message: String = StringBuilder().apply {
        appendln()
        appendln("===== SCOPE CYCLE FOUND ====")
    }.toString()
}