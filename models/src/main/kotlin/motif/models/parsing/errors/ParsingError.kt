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

class ScopeMustBeAnInterface(val scopeClass: IrClass) : ParsingError()
class InvalidScopeMethod(val method: IrMethod) : ParsingError()
class ObjectsFieldFound(val objectsClass: IrClass) : ParsingError()
class ObjectsConstructorFound(val objectsClass: IrClass) : ParsingError()
class VoidObjectsMethod(val method: IrMethod) : ParsingError()
class PrivateObjectsMethod(val method: IrMethod) : ParsingError()
class NullableFactoryMethod(val method: IrMethod) : ParsingError()
class NullableDependency(val parameter: IrParameter, val method: IrMethod) : ParsingError()
class InvalidObjectsMethod(val method: IrMethod) : ParsingError()
class TypeNotSpreadable(val type: IrType, val method: IrMethod) : ParsingError()
class NoSuitableConstructor(val type: IrType, val method: IrMethod) : ParsingError()
class NotAssignableBindsMethod(val method: IrMethod, val returnType: IrType, val parameterType: IrType) : ParsingError()
class VoidDependenciesMethod(val method: IrMethod) : ParsingError()
class DependencyMethodWithParameters(val method: IrMethod) : ParsingError()
class MissingInjectAnnotation(val type: IrType, val method: IrMethod) : ParsingError()