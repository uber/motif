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
import motif.compiler.ir.CompilerMethod
import motif.models.errors.NotExposedError
import javax.lang.model.element.Element

class NotExposedHandler : ErrorHandler<NotExposedError>() {

    override fun message(error: NotExposedError): String {
        val dependencyTable = AsciiTable(AT_Context()
                .setGrid(U8_Grids.borderStrongDoubleLight())
                .setWidth(60)).apply {
            addRule()
            addRow(error.requiredDependency.dependency).setPaddingLeft(1)
            addRule()
        }
        val scopeTable = AsciiTable(AT_Context()
                .setGrid(U8_Grids.borderStrongDoubleLight())
                .setWidth(60)).apply {
            addRule()
            addRow(error.scopeClass.ir.type.simpleName).setPaddingLeft(1)
            addRule()
        }
        val requiredByTable = AsciiTable(AT_Context()
                .setGrid(U8_Grids.borderStrongDoubleLight())
                .setWidth(60)).apply {
            addRule()
            error.requiredDependency.consumingScopes.forEach {
                addRow(it.simpleName).setPaddingLeft(1)
                addRule()
            }
        }
        return StringBuilder().apply {
            appendln("DEPENDENCY NOT EXPOSED:")
            appendln(dependencyTable.render())
            appendln("is not exposed by:")
            appendln(scopeTable.render())
            appendln("but is required by:")
            appendln(requiredByTable.render())
            appendln("Annotate the factory method with @Expose to allow descendant Scopes to consume the " +
                    "dependency. See https://github.com/uber/motif/wiki#expose for details.")
        }.toString()
    }

    override fun element(error: NotExposedError): Element {
        return (error.factoryMethod.ir as CompilerMethod).element
    }
}