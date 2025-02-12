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
package motif.intellij

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementFactory
import com.intellij.psi.util.parentsWithSelf
import motif.ast.IrClass
import motif.ast.IrType
import motif.ast.intellij.IntelliJClass
import motif.ast.intellij.IntelliJType
import motif.core.ResolvedGraph
import motif.models.ConstructorFactoryMethod
import motif.models.Scope

sealed class GraphState {

  object Uninitialized : GraphState()

  object Loading : GraphState()

  class Error(val graph: ResolvedGraph) : GraphState()

  class Valid(project: Project, val graph: ResolvedGraph) : GraphState() {

    val invalidator = GraphInvalidator(project, graph)
  }
}

class GraphManager(private val project: Project) : ProjectComponent {

  private val graphFactory = GraphFactory(project)

  private val listeners = mutableListOf<Listener>()

  private var graphState: GraphState = GraphState.Uninitialized

  override fun initComponent() {}

  override fun disposeComponent() {}

  fun refresh() {
    setGraphState(GraphState.Loading)
    ProgressManager.getInstance()
        .run(
            object : Task.Backgroundable(project, "Refresh") {

              override fun run(indicator: ProgressIndicator) {
                ApplicationManager.getApplication().runReadAction {
                  val graph = graphFactory.compute()
                  val state =
                      if (graph.errors.isEmpty()) {
                        GraphState.Valid(project, graph)
                      } else {
                        GraphState.Error(graph)
                      }
                  setGraphState(state)
                }
              }
            },
        )
  }

  fun addListener(listener: Listener) {
    listeners.add(listener)
    listener.onStateChange(graphState)
  }

  fun removeListener(listener: Listener) {
    listeners.remove(listener)
  }

  private fun setGraphState(graphState: GraphState) {
    this.graphState = graphState
    listeners.forEach { it.onStateChange(graphState) }
  }

  interface Listener {

    fun onStateChange(state: GraphState)
  }
}

class GraphInvalidator(private val project: Project, private val graph: ResolvedGraph) {

  private val psiElementFactory = PsiElementFactory.SERVICE.getInstance(project)

  private val relevantTypes: Set<IrType> by lazy {
    graph.scopes
        .flatMap { scope ->
          (listOfNotNull(scope.objects?.clazz) +
                  scope.clazz +
                  spreadClasses(scope) +
                  constructorClasses(scope))
              .flatMap { clazz -> typeAndSupertypes((clazz as IntelliJClass).psiClass) }
              .map { IntelliJType(project, psiElementFactory.createType(it)) }
        }
        .toSet()
  }

  fun shouldInvalidate(changedElement: PsiElement): Boolean =
      (sequenceOf(changedElement) + changedElement.parentsWithSelf)
          .mapNotNull { it as? PsiClass }
          .map { psiElementFactory.createType(it) }
          .any { IntelliJType(project, it) in relevantTypes }

  private fun spreadClasses(scope: Scope): List<IrClass> =
      scope.factoryMethods.mapNotNull { it.spread }.map { spread -> spread.clazz }

  private fun constructorClasses(scope: Scope): List<IrClass> =
      scope.factoryMethods.filterIsInstance<ConstructorFactoryMethod>().mapNotNull {
        it.returnType.type.type.resolveClass()
      }

  private fun typeAndSupertypes(psiClass: PsiClass): Set<PsiClass> {
    if (psiClass.qualifiedName == "java.lang.Object") {
      return emptySet()
    }
    return setOf(psiClass) + psiClass.supers.flatMap { typeAndSupertypes(it) }
  }
}
