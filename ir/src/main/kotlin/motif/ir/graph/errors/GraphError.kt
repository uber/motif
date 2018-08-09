package motif.ir.graph.errors

import de.vandermeer.asciitable.AT_Context
import de.vandermeer.asciitable.AsciiTable
import de.vandermeer.asciithemes.u8.U8_Grids
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment
import motif.ir.graph.DependencyCycle
import motif.ir.graph.DuplicateFactoryMethod
import motif.ir.graph.Node
import motif.ir.source.base.Dependency
import motif.ir.source.base.Type
import motif.ir.source.dependencies.Dependencies

sealed class GraphError {
    abstract val message: String
}

class DependencyCycleError(val cycles: List<DependencyCycle>) : GraphError() {

    override val message: String = StringBuilder().apply {
        appendln()
        appendln("===== DEPENDENCY CYCLE FOUND ====")
    }.toString()
}

class DuplicateFactoryMethodsError(val duplicates: List<DuplicateFactoryMethod>) : GraphError() {

    override val message: String = StringBuilder().apply {
        appendln()
        appendln("===== DUPLICATE FACTORY METHODS ====")
    }.toString()
}

class MissingDependenciesError(
        val requiredBy: Node,
        val dependencies: List<Dependency>) : GraphError() {

    private val tableContext = AT_Context()
            .setGrid(U8_Grids.borderStrongDoubleLight())

    override val message: String = AsciiTable(tableContext).apply {
        addRule()
        addRow("MISSING DEPENDENCIES").setTextAlignment(TextAlignment.CENTER)
        dependencies.forEach { dependency ->
            addRow(dependency).setPaddingLeft(1)
        }
    }.render()
}

class ScopeCycleError(val cycle: List<Type>) : GraphError() {

    override val message: String = StringBuilder().apply {
        appendln()
        appendln("===== SCOPE CYCLE FOUND ====")
    }.toString()
}