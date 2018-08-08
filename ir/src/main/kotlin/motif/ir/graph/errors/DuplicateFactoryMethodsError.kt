package motif.ir.graph.errors

import motif.ir.graph.DuplicateFactoryMethod

class DuplicateFactoryMethodsError(val duplicates: List<DuplicateFactoryMethod>) : GraphError {

    override val message: String = StringBuilder().apply {
        appendln()
        appendln("===== DUPLICATE FACTORY METHODS ====")
    }.toString()
}