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
import com.intellij.psi.PsiElement
import javax.swing.Icon
import motif.ast.intellij.IntelliJClass
import motif.ast.intellij.IntelliJMethod
import motif.ast.intellij.IntelliJMethodParameter
import motif.core.AlreadySatisfiedError
import motif.core.DependencyCycleError
import motif.core.ResolvedGraph
import motif.core.ScopeCycleError
import motif.core.UnexposedSourceError
import motif.core.UnsatisfiedDependencyError
import motif.errormessage.ErrorMessage
import motif.models.AccessMethodParameters
import motif.models.DependencyMethodWithParameters
import motif.models.DuplicatedChildParameterSource
import motif.models.InjectAnnotationRequired
import motif.models.InvalidFactoryMethod
import motif.models.InvalidQualifier
import motif.models.MotifError
import motif.models.NoSuitableConstructor
import motif.models.NotAssignableBindsMethod
import motif.models.NullableDynamicDependency
import motif.models.NullableFactoryMethod
import motif.models.NullableParameter
import motif.models.NullableSpreadMethod
import motif.models.ObjectsConstructorFound
import motif.models.ObjectsFieldFound
import motif.models.ScopeMustBeAnInterface
import motif.models.UnspreadableType
import motif.models.VoidDependenciesMethod
import motif.models.VoidFactoryMethod
import motif.models.VoidScopeMethod

open class ScopeHierarchyErrorDescriptor(
    project: Project,
    graph: ResolvedGraph,
    parentDescriptor: HierarchyNodeDescriptor?,
    val error: MotifError,
    val errorMessage: ErrorMessage,
) :
    ScopeHierarchyNodeDescriptor(
        project,
        graph,
        parentDescriptor,
        getElementFromError(error),
        false,
    ) {

  companion object {
    fun getElementFromError(error: MotifError): PsiElement =
        when (error) {
          is ScopeMustBeAnInterface -> {
            (error.scopeClass as IntelliJClass).psiClass
          }
          is VoidScopeMethod -> {
            (error.method as IntelliJMethod).psiMethod
          }
          is AccessMethodParameters -> {
            (error.scope.clazz as IntelliJClass).psiClass
          }
          is ObjectsFieldFound -> {
            (error.scope.clazz as IntelliJClass).psiClass
          }
          is ObjectsConstructorFound -> {
            (error.scope.clazz as IntelliJClass).psiClass
          }
          is VoidFactoryMethod -> {
            (error.method as IntelliJMethod).psiMethod
          }
          is NullableFactoryMethod -> {
            (error.method as IntelliJMethod).psiMethod
          }
          is NullableParameter -> {
            (error.parameter as IntelliJMethodParameter).psiParameter
          }
          is NullableDynamicDependency -> {
            (error.parameter as IntelliJMethodParameter).psiParameter
          }
          is InvalidFactoryMethod -> {
            (error.method as IntelliJMethod).psiMethod
          }
          is UnspreadableType -> {
            (error.method as IntelliJMethod).psiMethod
          }
          is NoSuitableConstructor -> {
            (error.method as IntelliJMethod).psiMethod
          }
          is InjectAnnotationRequired -> {
            (error.method as IntelliJMethod).psiMethod
          }
          is NotAssignableBindsMethod -> {
            (error.method as IntelliJMethod).psiMethod
          }
          is VoidDependenciesMethod -> {
            (error.method as IntelliJMethod).psiMethod
          }
          is DependencyMethodWithParameters -> {
            (error.method as IntelliJMethod).psiMethod
          }
          is NullableSpreadMethod -> {
            (error.spreadMethod as IntelliJMethod).psiMethod
          }
          is InvalidQualifier -> {
            (error.annotated.annotations[0].members[0] as IntelliJMethod).psiMethod
          }
          is DuplicatedChildParameterSource -> {
            (error.childScopeMethod.method as IntelliJMethod).psiMethod
          }
          is ScopeCycleError -> {
            (error.path[0].clazz as IntelliJClass).psiClass
          }
          is UnsatisfiedDependencyError -> {
            (error.top.clazz as IntelliJClass).psiClass
          }
          is DependencyCycleError -> {
            (error.path[0].scope.clazz as IntelliJClass).psiClass
          }
          is UnexposedSourceError -> {
            (error.source.scope.clazz as IntelliJClass).psiClass
          }
          is AlreadySatisfiedError -> {
            (error.scope.clazz as IntelliJClass).psiClass
          }
          else -> throw UnsupportedOperationException()
        }
  }

  override fun updateText(text: CompositeAppearance) {
    text.ending.addText(errorMessage.name)
  }

  override fun getIcon(element: PsiElement): Icon? = AllIcons.RunConfigurations.TestFailed

  override fun getLegend(): String? = errorMessage.text

  override fun toString(): String = errorMessage.name
}
