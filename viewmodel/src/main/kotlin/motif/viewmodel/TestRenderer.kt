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
package motif.viewmodel

import motif.core.ResolvedGraph
import motif.errormessage.ErrorMessage
import motif.models.*

object TestRenderer {

    @JvmStatic
    fun render(graph: ResolvedGraph): String {
        val graphViewModel = GraphViewModel.create(graph)
        return if (graph.errors.isEmpty()) {
            StringBuilder().apply {
                graphViewModel.rootScopes.forEach { rootScopeViewModel ->
                    renderScope(0, rootScopeViewModel)
                }
            }.toString()
        } else {
            ErrorMessage.toString(graph)
        }
    }

    private fun StringBuilder.renderScope(indent: Int, scopeViewModel: ScopeViewModel) {
        val scopeName = scopeViewModel.scope.simpleName
        val scopeLine = " ${"-".repeat(scopeName.length + 2)}"
        appendln(indent, scopeLine)
        appendln(indent, "| $scopeName |")
        appendln(indent, scopeLine)
        appendln()
        renderRequired(indent + 1, scopeViewModel.requiredDependencies)
        renderProvided(indent + 1, scopeViewModel.providedDependencies)
        scopeViewModel.children.forEach { child -> renderScope(indent + 1, child) }
    }

    private fun StringBuilder.renderRequired(indent: Int, requiredDependencies: List<RequiredDependency>, topLevel: Boolean = true) {
        var header = "Required"
        header = if (topLevel) "==== $header ====" else "[ $header ]"
        appendln(indent, header)
        if (topLevel) appendln()
        requiredDependencies.forEach { requiredDependency -> renderRequired(indent + 1, requiredDependency, topLevel) }
    }

    private fun StringBuilder.renderProvided(indent: Int, providedDependencies: List<ProvidedDependency>) {
        appendln(indent, "==== Provides ====")
        appendln()
        providedDependencies.forEach { providedDependency -> renderProvided(indent + 1, providedDependency) }
    }

    private fun StringBuilder.renderRequired(indent: Int, requiredDependency: RequiredDependency, topLevel: Boolean) {
        var header = requiredDependency.type.simpleName
        header = if (topLevel) "---- $header ----" else header
        appendln(indent, header)
        renderProvidedBy(indent + 1, requiredDependency.providedBy)
        if (topLevel) {
            renderConsumedBy(indent + 1, requiredDependency.requiredBy)
        }
        if (topLevel) appendln()
    }

    private fun StringBuilder.renderProvided(indent: Int, providedDependency: ProvidedDependency) {
        appendln(indent, "---- ${toString(providedDependency.source, showType = true)} ----")
        renderRequired(indent + 1, providedDependency.requiredDependencies, false)
        renderConsumedBy(indent + 1, providedDependency.consumedBy)
        appendln()
    }

    private fun StringBuilder.renderProvidedBy(indent: Int, providedBy: List<Source>) {
        appendln(indent, "[ Provided By ]")
        providedBy.forEach { source ->
            appendln(indent + 1, "* ${toString(source)}")
        }
    }

    private fun StringBuilder.renderConsumedBy(indent: Int, consumedBy: List<Sink>) {
        appendln(indent, "[ Consumed By ]")
        consumedBy.forEach { sink ->
            appendln(indent + 1, "* ${toString(sink)}")
        }
    }

    private fun toString(source: Source, showType: Boolean = false): String {
        val referenceString = when (source) {
            is ChildParameterSource -> "${source.scope.simpleName}.${source.parameter.method.method.name}(${source.parameter.parameter.name})"
            is FactoryMethodSource -> "${source.scope.objects!!.clazz.simpleName}.${source.factoryMethod.name}"
            is ScopeSource -> "implicit"
            is SpreadSource -> "${source.scope.objects!!.clazz.simpleName}.${source.spreadMethod.spread.factoryMethod.name}"
        }
        val prefix = if (showType) source.type.simpleName else source.scope.simpleName
        return "$prefix | $referenceString"
    }

    private fun toString(sink: Sink): String {
        val referenceString = when (sink) {
            is AccessMethodSink -> "${sink.scope.simpleName}.${sink.accessMethod.method.name}()"
            is FactoryMethodSink -> "${sink.scope.objects!!.clazz.simpleName}.${sink.parameter.factoryMethod.name}(${sink.parameter.parameter.name})"
        }
        return "${sink.scope.simpleName} | $referenceString"
    }

    private fun StringBuilder.appendln(count: Int, value: String) {
        append("  ".repeat(count))
        appendln(value)
    }
}