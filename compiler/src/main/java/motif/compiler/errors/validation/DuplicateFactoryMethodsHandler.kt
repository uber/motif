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
import motif.ast.compiler.CompilerMethod
import motif.models.errors.DuplicateFactoryMethodsError
import javax.lang.model.element.Element

class DuplicateFactoryMethodsHandler : ErrorHandler<DuplicateFactoryMethodsError>() {

    override fun message(error: DuplicateFactoryMethodsError): String {
        val dependencyTable = AsciiTable(AT_Context()
                .setGrid(U8_Grids.borderStrongDoubleLight())
                .setWidth(60)).apply {
            addRule()
            addRow(error.duplicate.providedDependency).setPaddingLeft(1)
            addRule()
        }
        val scopeTable = AsciiTable(AT_Context()
                .setGrid(U8_Grids.borderStrongDoubleLight())
                .setWidth(60)).apply {
            addRule()
            addRow(error.duplicate.scopeType.simpleName).setPaddingLeft(1)
            addRule()
        }
        val otherScopesTable = AsciiTable(AT_Context()
                .setGrid(U8_Grids.borderStrongDoubleLight())
                .setWidth(60)).apply {
            addRule()
            error.existing.forEach { scopeType ->
                addRow(scopeType.simpleName).setPaddingLeft(1)
                addRule()
            }
        }
        return StringBuilder().apply {
            appendln("DUPLICATE FACTORY METHOD:")
            appendln(dependencyTable.render())
            appendln("was declared in:")
            appendln(scopeTable.render())
            appendln("and in the following Scopes:")
            appendln(otherScopesTable.render())
        }.toString()
    }

    override fun element(error: DuplicateFactoryMethodsError): Element {
        return (error.duplicate.ir as CompilerMethod).element
    }
}