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

import motif.core.*
import motif.models.*

internal interface ErrorHandler {

    val name: String

    /**
     * The message that the compiler will output.
     */
    fun StringBuilder.handle()

    companion object {

        fun get(graph: ResolvedGraph, error: MotifError): ErrorHandler {
            return when (error) {
                is ParsingError -> when (error) {
                    is ScopeMustBeAnInterface -> ScopeMustBeAnInterfaceHandler(error)
                    is InvalidScopeMethod -> InvalidScopeMethodHandler(error)
                    is ObjectsFieldFound -> ObjectsFieldFoundHandler(error)
                    is ObjectsConstructorFound -> ObjectsConstructorFoundHandler(error)
                    is VoidFactoryMethod -> VoidFactoryMethodHandler(error)
                    is NullableFactoryMethod -> NullableFactoryMethodHandler(error)
                    is NullableParameter -> NullableParameterHandler(error)
                    is NullableDynamicDependency -> NullableDynamicDependencyHandler(error)
                    is NullableDependencyMethod -> NullableDependencyMethodHandler(error)
                    is InvalidFactoryMethod -> InvalidFactoryMethodHandler(error)
                    is UnspreadableType -> UnspreadableTypeHandler(error)
                    is NoSuitableConstructor -> NoSuitableConstructorHandler(error)
                    is InjectAnnotationRequired -> InjectAnnotationRequiredHandler(error)
                    is NotAssignableBindsMethod -> NotAssignableBindsMethodHandler(error)
                    is VoidDependenciesMethod -> VoidDependenciesMethodHandler(error)
                    is DependencyMethodWithParameters -> DependencyMethodWithParametersHandler(error)
                    is NullableSpreadMethod -> NullableSpreadMethodHandler(error)
                    is InvalidScopeFactoryTypeArgument -> InvalidScopeFactoryTypeArgumentHandler(error)
                    is UnannotatedScopeFactoryScope -> UnannotatedScopeFactoryScopeHandler(error)
                }
                is ProcessingError -> when (error) {
                    is ScopeCycleError -> ScopeCycleHandler(error)
                    is UnsatisfiedDependencyError -> UnsatisfiedDependencyHandler(error)
                    is UnusedDependencyError -> UnusedDependencyHandler(error)
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
