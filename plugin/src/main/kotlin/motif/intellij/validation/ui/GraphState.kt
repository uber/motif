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
package motif.intellij.validation.ui

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementFactory
import motif.intellij.validation.index.ScopeIndex
import motif.intellij.validation.ir.IntelliJType
import motif.intellij.validation.ui.error.ErrorHandler
import motif.intellij.validation.ui.error.InvalidScopeMethodHandler
import motif.intellij.validation.ui.error.NoOpErrorHandler
import motif.models.errors.*
import motif.models.graph.Graph
import motif.models.graph.GraphFactory
import motif.models.java.IrType

object GraphStateManager {

    private val listeners: MutableList<(GraphState) -> Unit> = mutableListOf()

    private var state: GraphState = GraphState(Graph(mapOf(), listOf(), null), listOf())

    @Synchronized
    fun refresh(project: Project): GraphState {
        val scopeClasses: List<PsiClass> = ScopeIndex.getInstance().getScopeClasses(project)
        val scopeAnnotatedTypes: Set<IrType> = scopeClasses
                .map { psiClass ->
                    val psiClassType: PsiClassType = PsiElementFactory.SERVICE.getInstance(project).createType(psiClass)
                    IntelliJType(project, psiClassType)
                }
                .toSet()

        val graph = GraphFactory.create(scopeAnnotatedTypes)
        val errors: List<GraphError> = graph.errors.flatMap { getHandler(it).handle(graph, it) }
        val state = GraphState(graph, errors)
        setState(state)
        return state
    }

    @Synchronized
    fun addListener(disposable: Disposable, listener: (GraphState) -> Unit) {
        Disposer.register(disposable, Disposable {
            listeners.remove(listener)
        })
        listeners.add(listener)
        ApplicationManager.getApplication().invokeLater {
            listener(state)
        }
    }

    private fun setState(state: GraphState) {
        this.state = state

        val listenersCopy = listeners.toList()
        ApplicationManager.getApplication().invokeLater {
            listenersCopy.forEach { listener -> listener(state) }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : MotifError> getHandler(error: T) : ErrorHandler<T> {
        // Necessary to fix exhaustive when check. https://youtrack.jetbrains.net/issue/KT-28391
        val motifError: MotifError = error
        val handler: ErrorHandler<*> = when (motifError) {
            is MissingDependenciesError -> NoOpErrorHandler.create()
            is ScopeCycleError -> NoOpErrorHandler.create()
            is DependencyCycleError -> NoOpErrorHandler.create()
            is DuplicateFactoryMethodsError -> NoOpErrorHandler.create()
            is NotExposedError -> NoOpErrorHandler.create()
            is NotExposedDynamicError -> NoOpErrorHandler.create()
            is ScopeMustBeAnInterface -> NoOpErrorHandler.create()
            is InvalidScopeMethod -> InvalidScopeMethodHandler()
            is ObjectsFieldFound -> NoOpErrorHandler.create()
            is ObjectsConstructorFound -> NoOpErrorHandler.create()
            is VoidObjectsMethod -> NoOpErrorHandler.create()
            is NullableFactoryMethod -> NoOpErrorHandler.create()
            is NullableDependency -> NoOpErrorHandler.create()
            is InvalidObjectsMethod -> NoOpErrorHandler.create()
            is TypeNotSpreadable -> NoOpErrorHandler.create()
            is NoSuitableConstructor -> NoOpErrorHandler.create()
            is NotAssignableBindsMethod -> NoOpErrorHandler.create()
            is VoidDependenciesMethod -> NoOpErrorHandler.create()
            is DependencyMethodWithParameters -> NoOpErrorHandler.create()
            is MissingInjectAnnotation -> NoOpErrorHandler.create()
        }
        return handler as ErrorHandler<T>
    }
}

class GraphState(val graph: Graph, val errors: List<GraphError>)

class GraphError(val psiElement: PsiElement?, val message: String)