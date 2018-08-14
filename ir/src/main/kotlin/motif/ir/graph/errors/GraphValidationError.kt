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