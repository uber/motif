package motif.ir.graph.errors

import motif.ir.graph.DependencyCycle

class DependencyCycleError(val cycles: List<DependencyCycle>) : GraphError {

    override val message: String = StringBuilder().apply {
        appendln()
        appendln("===== DEPENDENCY CYCLE FOUND ====")
    }.toString()
}