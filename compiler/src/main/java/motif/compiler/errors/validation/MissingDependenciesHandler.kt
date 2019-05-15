/*
 * Copyright (c) 2018-2019 Uber Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package motif.compiler.errors.validation

import de.vandermeer.asciitable.AT_Context
import de.vandermeer.asciitable.AsciiTable
import de.vandermeer.asciithemes.u8.U8_Grids
import motif.compiler.errors.ErrorHandler
import motif.ast.compiler.CompilerClass
import motif.models.errors.MissingDependenciesError
import javax.lang.model.element.Element

class MissingDependenciesHandler : ErrorHandler<MissingDependenciesError>() {

    override fun message(error: MissingDependenciesError): String {
        val scopeTable = AsciiTable(AT_Context()
                .setGrid(U8_Grids.borderStrongDoubleLight())
                .setWidth(60)).apply {
            addRule()
            addRow(error.requiredBy.scopeClass.ir.type.simpleName).setPaddingLeft(1)
            addRule()
        }
        val table = AsciiTable(AT_Context()
                .setGrid(U8_Grids.borderStrongDoubleLight())
                .setWidth(60)).apply {
            addRule()
            error.dependencies.forEach { dependency ->
                addRow(dependency).setPaddingLeft(1)
                addRule()
            }
        }
        return StringBuilder().apply {
            appendln("MISSING DEPENDENCIES:")
            appendln(scopeTable.render())
            appendln("is missing the following dependencies:")
            appendln(table.render())
        }.toString()
    }

    override fun element(error: MissingDependenciesError): Element? {
        return (error.requiredBy.scopeClass.ir as CompilerClass).declaredType.asElement()
    }
}
