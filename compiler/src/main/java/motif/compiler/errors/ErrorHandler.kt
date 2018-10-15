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
package motif.compiler.errors

import motif.compiler.errors.parsing.*
import motif.compiler.errors.validation.*
import motif.models.graph.errors.*
import motif.models.parsing.errors.*
import javax.lang.model.element.Element

abstract class ErrorHandler<T> {

    abstract fun message(error: T): String
    abstract fun element(error: T): Element?

    fun error(error: T): ErrorMessage {
        return ErrorMessage(element(error), message(error))
    }

    companion object {

        fun handle(error: GraphError): ErrorMessage {
            return when (error) {
                is MissingDependenciesError -> MissingDependenciesHandler().error(error)
                is ScopeCycleError -> ScopeCycleHandler().error(error)
                is DependencyCycleError -> DependencyCycleHandler().error(error)
                is DuplicateFactoryMethodsError -> DuplicateFactoryMethodsHandler().error(error)
                is NotExposedError -> NotExposedHandler().error(error)
            }
        }

        fun handle(error: ParsingError): ErrorMessage {
            return when(error) {
                is ScopeMustBeAnInterface -> ScopeMustBeAnInterfaceHandler().error(error)
                is InvalidScopeMethod -> InvalidScopeMethodHandler().error(error)
                is ObjectsFieldFound -> ObjectsFieldFoundHandler().error(error)
                is ObjectsConstructorFound -> ObjectsConstructorFoundHandler().error(error)
                is VoidObjectsMethod -> VoidObjectsMethodHandler().error(error)
                is PrivateObjectsMethod -> PrivateObjectsMethodHandler().error(error)
                is NullableFactoryMethod -> NullableFactoryMethodHandler().error(error)
                is NullableDependency -> NullableDependencyHandler().error(error)
                is InvalidObjectsMethod -> InvalidObjectsMethodHandler().error(error)
                is TypeNotSpreadable -> TypeNotSpreadableHandler().error(error)
                is NoSuitableConstructor -> NoSuitableConstructorHandler().error(error)
                is NotAssignableBindsMethod -> NotAssignableBindsMethodHandler().error(error)
                is VoidDependenciesMethod -> VoidDependenciesMethodHandler().error(error)
                is DependencyMethodWithParameters -> DependencyMethodWithParametersHandler().error(error)
                is MissingInjectAnnotation -> MissingInjectAnnotationHandler().error(error)
            }
        }
    }
}