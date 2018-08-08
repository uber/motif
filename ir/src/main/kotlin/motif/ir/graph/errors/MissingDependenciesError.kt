package motif.ir.graph.errors

import de.vandermeer.asciitable.AT_Context
import de.vandermeer.asciitable.AsciiTable
import de.vandermeer.asciithemes.u8.U8_Grids
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment
import motif.ir.source.dependencies.Dependencies

class MissingDependenciesError(val dependencies: Dependencies) : GraphError {

    private val tableContext = AT_Context()
            .setGrid(U8_Grids.borderStrongDoubleLight())
    override val message: String = AsciiTable(tableContext).apply {
        addRule()
        addRow(null, "MISSING DEPENDENCIES").setTextAlignment(TextAlignment.CENTER)
        addRule()
        addRow("REQUIRED BY", "DEPENDENCY").setTextAlignment(TextAlignment.CENTER)
        addRule()
        dependencies.scopeToDependencies.forEach { scope, dependencies ->
            addRow(scope.simpleName, dependencies[0]).setPaddingLeft(1)
            dependencies.drop(1).forEach {
                addRow("", it).setPaddingLeft(1)
            }
            addRule()
        }
    }.render()
}