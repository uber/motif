package motif.compiler.errors

import de.vandermeer.asciitable.AT_Context
import de.vandermeer.asciitable.AsciiTable
import de.vandermeer.asciithemes.u8.U8_Grids
import motif.ir.graph.errors.MissingDependenciesError
import javax.lang.model.element.Element
import javax.lang.model.type.DeclaredType

class MissingDependenciesHandler : ErrorHandler<MissingDependenciesError>() {

    override fun message(error: MissingDependenciesError): String {
        val table = AsciiTable(AT_Context()
                .setGrid(U8_Grids.borderStrongDoubleLight())
                .setWidth(60)).apply {
            addRule()
            error.dependencies.list.forEach { annotatedDependency ->
                addRow(annotatedDependency.dependency).setPaddingLeft(1)
                addRule()
            }
        }
        return StringBuilder().apply {
            appendln("MISSING DEPENDENCIES:")
            appendln(error.requiredBy.scopeClass.type)
            appendln(table.render())
        }.toString()
    }

    override fun element(error: MissingDependenciesError): Element? {
        return (error.requiredBy.scopeClass.type.userData as DeclaredType).asElement()
    }
}