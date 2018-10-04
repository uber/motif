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
package motif.models.parsing.errors

import motif.models.java.IrClass
import motif.models.java.IrMethod
import motif.models.java.IrParameter
import motif.models.java.IrType

sealed class ParsingError : RuntimeException()

class ScopeMustBeAnInterface(scopeClass: IrClass) : ParsingError()
class InvalidScopeMethodError(method: IrMethod) : ParsingError()
class ObjectsFieldFound : ParsingError()
class ObjectsConstructorFound : ParsingError()
class VoidObjectsMethod(method: IrMethod) : ParsingError()
class PrivateObjectsMethod(method: IrMethod) : ParsingError()
class NullableFactoryMethod(method: IrMethod) : ParsingError()
class NullableDependency(parameter: IrParameter, method: IrMethod) : ParsingError()
class InvalidObjectsMethod(method: IrMethod) : ParsingError()
class TypeNotSpreadable(type: IrType, method: IrMethod) : ParsingError()
class NoSuitableConstructor(type: IrType, method: IrMethod) : ParsingError()
class NotAssignableBindsMethod(method: IrMethod, returnType: IrType, parameterType: IrType) : ParsingError()
class VoidDependenciesMethod(method: IrMethod) : ParsingError()
class DependencyMethodWithParameters(method: IrMethod) : ParsingError()
class MissingInjectAnnotation(type: IrType) : ParsingError()