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
package motif.models

import motif.ast.IrClass
import motif.ast.IrMethod
import motif.ast.IrParameter
import motif.ast.IrType

sealed class ParsingError : RuntimeException(), MotifError

class ScopeMustBeAnInterface(val scopeClass: IrClass) : ParsingError()
class InvalidScopeMethod(val scope: Scope, val method: IrMethod) : ParsingError()
class ObjectsFieldFound(val scope: Scope, val objectClass: IrClass) : ParsingError()
class ObjectsConstructorFound(val scope: Scope, val objectClass: IrClass) : ParsingError()
class VoidFactoryMethod(val objects: Objects, val method: IrMethod) : ParsingError()
class NullableFactoryMethod(val objects: Objects, val method: IrMethod) : ParsingError()
class NullableParameter(val scope: Scope, val owner: IrClass, val method: IrMethod, val parameter: IrParameter) : ParsingError()
class NullableDynamicDependency(val scope: Scope, val method: IrMethod, val parameter: IrParameter) : ParsingError()
class NullableDependencyMethod(val dependenciesClass: IrClass, val method: IrMethod) : ParsingError()
class InvalidFactoryMethod(val objects: Objects, val method: IrMethod) : ParsingError()
class UnspreadableType(val objects: Objects, val method: IrMethod, val type: IrType) : ParsingError()
class NoSuitableConstructor(val objects: Objects, val method: IrMethod, val type: IrType) : ParsingError()
class InjectAnnotationRequired(val objects: Objects, val method: IrMethod, val type: IrType) : ParsingError()
class NotAssignableBindsMethod(val objects: Objects, val method: IrMethod, val returnType: IrType, val parameterType: IrType) : ParsingError()
class VoidDependenciesMethod(val dependenciesClass: IrClass, val method: IrMethod) : ParsingError()
class DependencyMethodWithParameters(val dependenciesClass: IrClass, val method: IrMethod) : ParsingError()
class NullableSpreadMethod(val objects: Objects, val factoryMethod: IrMethod, val spreadClass: IrClass, val spreadMethod: IrMethod) : ParsingError()
class InvalidScopeFactoryTypeArgument(val scopeFactoryClass: IrClass, val typeArgument: IrType) : ParsingError()
class UnannotatedScopeFactoryScope(val scopeFactoryClass: IrClass, val scopeClass: IrClass) : ParsingError()