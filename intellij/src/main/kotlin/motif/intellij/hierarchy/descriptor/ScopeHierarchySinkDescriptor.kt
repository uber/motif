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
package motif.intellij.hierarchy.descriptor

import com.intellij.icons.AllIcons
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ui.util.CompositeAppearance
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import javax.swing.Icon
import motif.ast.IrMethod
import motif.ast.intellij.IntelliJMethod
import motif.core.ResolvedGraph
import motif.models.AccessMethodSink
import motif.models.FactoryMethodSink
import motif.models.FactoryMethodSource
import motif.models.Sink
import motif.models.Type

open class ScopeHierarchySinkDescriptor(
    project: Project,
    graph: ResolvedGraph,
    parentDescriptor: HierarchyNodeDescriptor?,
    val sink: Sink,
) :
    ScopeHierarchyNodeDescriptor(
        project,
        graph,
        parentDescriptor,
        getElementFromSink(sink),
        false,
    ) {

  companion object {
    fun getElementFromSink(sink: Sink): PsiElement =
        when (sink) {
          is FactoryMethodSink -> {
            (sink.parameter.factoryMethod.method as IntelliJMethod).psiMethod
          }
          is AccessMethodSink -> {
            (sink.accessMethod.method as IntelliJMethod).psiMethod
          }
        }

    fun getConsumingTypeFromSink(sink: Sink): Type =
        when (sink) {
          is FactoryMethodSink -> {
            sink.parameter.factoryMethod.returnType.type
          }
          is AccessMethodSink -> {
            sink.type
          }
        }
  }

  override fun updateText(text: CompositeAppearance) {
    text.ending.addText(sink.type.simpleName)
    val consumingType = getConsumingTypeFromSink(sink)
    text.ending.addText(" → ${consumingType.simpleName}", getPackageNameAttributes())
  }

  override fun getIcon(element: PsiElement): Icon? =
      if (element is PsiClass && element.isInterface) {
        AllIcons.Nodes.Interface
      } else {
        AllIcons.Nodes.Class
      }

  override fun getLegend(): String? {
    val sb: StringBuilder = StringBuilder()
    when (sink) {
      is FactoryMethodSink -> {
        val method: IrMethod = sink.parameter.method
        if (method.isConstructor) {
          sb.append(
              "Object <b>" +
                  method.name +
                  "</b> used in Scope <b>" +
                  sink.scope.simpleName +
                  "</b>" +
                  " has a dependency on type <b>" +
                  sink.type.simpleName +
                  "</b>.",
          )
        }
        graph.getProviders(sink).forEach { source ->
          when (source) {
            is FactoryMethodSource -> {
              sb.append(
                  "This dependency is provided by Scope <b>" +
                      source.scope.simpleName +
                      "</b>" +
                      ", via Motif Factory method <b>" +
                      source.factoryMethod.name +
                      "()</b>.",
              )
            }
            else -> {
              // TODO : Handle all types
            }
          }
        }
        return sb.toString()
      }
      is AccessMethodSink -> {
        // TODO : Handle all types
      }
    }
    return null
  }

  override fun toString(): String = sink.type.simpleName
}
