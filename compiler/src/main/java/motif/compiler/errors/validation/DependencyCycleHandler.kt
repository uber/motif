/*
 * Copyright (c) 2018 Uber Technologies, Inc.
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
import motif.compiler.ir.CompilerClass
import motif.models.errors.DependencyCycleError
import javax.lang.model.element.Element

class DependencyCycleHandler : ErrorHandler<DependencyCycleError>() {

    override fun message(error: DependencyCycleError): String {
        val scopeTable = AsciiTable(AT_Context()
                .setGrid(U8_Grids.borderStrongDoubleLight())
                .setWidth(60)).apply {
            addRule()
            addRow(error.scopeClass.ir.type.simpleName).setPaddingLeft(1)
            addRule()
        }
        val table = AsciiTable(AT_Context()
                .setGrid(U8_Grids.borderStrongDoubleLight())
                .setWidth(60)).apply {
            addRule()

            (error.cycle + error.cycle.first()).forEachIndexed { i, factoryMethod ->
                val prefix = if (i == 0) "" else "-> "
                addRow("$prefix${factoryMethod.providedDependency}").setPaddingLeft(1)
                addRule()
            }
        }
        return StringBuilder().apply {
            appendln("DEPENDENCY CYCLE FOUND:")
            appendln(scopeTable.render())
            appendln("contains the following dependency cycle:")
            appendln(table.render())
        }.toString()
    }

    override fun element(error: DependencyCycleError): Element? {
        return (error.scopeClass.ir as CompilerClass).declaredType.asElement()
    }
}