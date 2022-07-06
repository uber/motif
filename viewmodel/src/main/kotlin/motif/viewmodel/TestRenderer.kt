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
import motif.models.AccessMethodSink
import motif.models.ChildParameterSource
import motif.models.FactoryMethodSink
import motif.models.FactoryMethodSource
import motif.models.ScopeSource
import motif.models.Sink
import motif.models.Source
import motif.models.SpreadSource

object TestRenderer {

  @JvmStatic
  fun render(graph: ResolvedGraph): String {
    val graphViewModel = GraphViewModel.create(graph)
    return StringBuilder()
        .apply {
          graphViewModel.rootScopes.forEach { rootScopeViewModel ->
            renderScope(0, rootScopeViewModel)
          }
        }
        .toString()
  }

  private fun StringBuilder.renderScope(indent: Int, scopeViewModel: ScopeViewModel) {
    val scopeName = scopeViewModel.scope.simpleName
    val scopeLine = " ${"-".repeat(scopeName.length + 2)}"
    appendLine(indent, scopeLine)
    appendLine(indent, "| $scopeName |")
    appendLine(indent, scopeLine)
    appendLine()
    renderRequired(indent + 1, scopeViewModel.requiredDependencies)
    renderProvided(indent + 1, scopeViewModel.providedDependencies)
    scopeViewModel.children.forEach { child -> renderScope(indent + 1, child) }
  }

  private fun StringBuilder.renderRequired(
      indent: Int,
      requiredDependencies: List<RequiredDependency>,
      topLevel: Boolean = true
  ) {
    var header = "Required"
    header = if (topLevel) "==== $header ====" else "[ $header ]"
    appendLine(indent, header)
    if (topLevel) appendLine()
    requiredDependencies.forEach { requiredDependency ->
      renderRequired(indent + 1, requiredDependency, topLevel)
    }
  }

  private fun StringBuilder.renderProvided(
      indent: Int,
      providedDependencies: List<ProvidedDependency>
  ) {
    appendLine(indent, "==== Provides ====")
    appendLine()
    providedDependencies.forEach { providedDependency ->
      renderProvided(indent + 1, providedDependency)
    }
  }

  private fun StringBuilder.renderRequired(
      indent: Int,
      requiredDependency: RequiredDependency,
      topLevel: Boolean
  ) {
    var header = requiredDependency.type.simpleName
    header = if (topLevel) "---- $header ----" else header
    appendLine(indent, header)
    renderProvidedBy(indent + 1, requiredDependency.providedBy)
    if (topLevel) {
      renderConsumedBy(indent + 1, requiredDependency.requiredBy)
    }
    if (topLevel) appendLine()
  }

  private fun StringBuilder.renderProvided(indent: Int, providedDependency: ProvidedDependency) {
    appendLine(indent, "---- ${toString(providedDependency.source, showType = true)} ----")
    renderRequired(indent + 1, providedDependency.requiredDependencies, false)
    renderConsumedBy(indent + 1, providedDependency.consumedBy)
    appendLine()
  }

  private fun StringBuilder.renderProvidedBy(indent: Int, providedBy: List<Source>) {
    appendLine(indent, "[ Provided By ]")
    providedBy.forEach { source -> appendLine(indent + 1, "* ${toString(source)}") }
  }

  private fun StringBuilder.renderConsumedBy(indent: Int, consumedBy: List<Sink>) {
    appendLine(indent, "[ Consumed By ]")
    consumedBy.forEach { sink -> appendLine(indent + 1, "* ${toString(sink)}") }
  }

  private fun toString(source: Source, showType: Boolean = false): String {
    val referenceString =
        when (source) {
          is ChildParameterSource ->
              "${source.scope.simpleName}.${source.parameter.method.method.name}(${source.parameter.parameter.name})"
          is FactoryMethodSource ->
              "${source.scope.objects!!.clazz.simpleName}.${source.factoryMethod.name}"
          is ScopeSource -> "implicit"
          is SpreadSource ->
              "${source.scope.objects!!.clazz.simpleName}.${source.spreadMethod.spread.factoryMethod.name}"
        }
    val prefix = if (showType) source.type.simpleName else source.scope.simpleName
    return "$prefix | $referenceString"
  }

  private fun toString(sink: Sink): String {
    val referenceString =
        when (sink) {
          is AccessMethodSink -> "${sink.scope.simpleName}.${sink.accessMethod.method.name}()"
          is FactoryMethodSink ->
              "${sink.scope.objects!!.clazz.simpleName}.${sink.parameter.factoryMethod.name}(${sink.parameter.parameter.name})"
        }
    return "${sink.scope.simpleName} | $referenceString"
  }

  private fun StringBuilder.appendLine(count: Int, value: String) {
    append("  ".repeat(count))
    this.appendLine(value)
  }
}
