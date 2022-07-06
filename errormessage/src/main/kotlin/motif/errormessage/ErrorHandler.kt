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
package motif.errormessage

import motif.core.AlreadySatisfiedError
import motif.core.DependencyCycleError
import motif.core.ProcessingError
import motif.core.ScopeCycleError
import motif.core.UnexposedSourceError
import motif.core.UnsatisfiedDependencyError
import motif.models.AccessMethodParameters
import motif.models.CannotResolveType
import motif.models.DependencyMethodWithParameters
import motif.models.DuplicatedChildParameterSource
import motif.models.DuplicatedDependenciesMethod
import motif.models.InjectAnnotationRequired
import motif.models.InvalidFactoryMethod
import motif.models.InvalidQualifier
import motif.models.MotifError
import motif.models.NoSuitableConstructor
import motif.models.Node
import motif.models.NotAssignableBindsMethod
import motif.models.NullableDynamicDependency
import motif.models.NullableFactoryMethod
import motif.models.NullableParameter
import motif.models.NullableSpreadMethod
import motif.models.ObjectsConstructorFound
import motif.models.ObjectsFieldFound
import motif.models.ParsingError
import motif.models.ScopeExtendsScope
import motif.models.ScopeMustBeAnInterface
import motif.models.UnspreadableType
import motif.models.VoidDependenciesMethod
import motif.models.VoidFactoryMethod
import motif.models.VoidScopeMethod

internal interface ErrorHandler {

  val name: String

  /** The message that the compiler will output. */
  fun StringBuilder.handle()

  companion object {

    fun get(error: MotifError): ErrorHandler {
      return when (error) {
        is ParsingError ->
            when (error) {
              is ScopeMustBeAnInterface -> ScopeMustBeAnInterfaceHandler(error)
              is VoidScopeMethod -> VoidScopeMethodHandler(error)
              is AccessMethodParameters -> AccessMethodParametersHandler(error)
              is ObjectsFieldFound -> ObjectsFieldFoundHandler(error)
              is ObjectsConstructorFound -> ObjectsConstructorFoundHandler(error)
              is VoidFactoryMethod -> VoidFactoryMethodHandler(error)
              is NullableFactoryMethod -> NullableFactoryMethodHandler(error)
              is NullableParameter -> NullableParameterHandler(error)
              is NullableDynamicDependency -> NullableDynamicDependencyHandler(error)
              is InvalidFactoryMethod -> InvalidFactoryMethodHandler(error)
              is UnspreadableType -> UnspreadableTypeHandler(error)
              is NoSuitableConstructor -> NoSuitableConstructorHandler(error)
              is InjectAnnotationRequired -> InjectAnnotationRequiredHandler(error)
              is NotAssignableBindsMethod -> NotAssignableBindsMethodHandler(error)
              is VoidDependenciesMethod -> VoidDependenciesMethodHandler(error)
              is DependencyMethodWithParameters -> DependencyMethodWithParametersHandler(error)
              is NullableSpreadMethod -> NullableSpreadMethodHandler(error)
              is InvalidQualifier -> InvalidQualifierHandler(error)
              is DuplicatedChildParameterSource -> DuplicatedChildParameterSourceHandler(error)
              is DuplicatedDependenciesMethod -> DuplicatedDependenciesMethodHandler(error)
              is ScopeExtendsScope -> ScopeExtendsScopeMethodHandler(error)
              is CannotResolveType -> CannotResolveTypeHandler(error)
            }
        is ProcessingError ->
            when (error) {
              is ScopeCycleError -> ScopeCycleHandler(error)
              is UnsatisfiedDependencyError -> UnsatisfiedDependencyHandler(error)
              is DependencyCycleError -> DependencyCycleHandler(error)
              is UnexposedSourceError -> UnexposedSourceHandler(error)
              is AlreadySatisfiedError -> AlreadySatisfiedHandler(error)
            }
        else -> throw IllegalStateException("Unknown error type: $${this::class.java.name}")
      }
    }
  }
}

internal val Node.errorText: String
  get() = NodeHandler.handle(this)
