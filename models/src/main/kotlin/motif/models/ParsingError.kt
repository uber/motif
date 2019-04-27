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
package motif.models

import motif.ast.IrClass
import motif.ast.IrMethod
import motif.ast.IrParameter
import motif.ast.IrType

sealed class ParsingError : RuntimeException(), MotifError

class ScopeMustBeAnInterface(val scopeClass: IrClass) : ParsingError() {

    override val humanReadable = "Scope must be an interface: ${scopeClass.qualifiedName}"

    override val testString = scopeClass.simpleName
}

class InvalidScopeMethod(val scope: Scope, val method: IrMethod) : ParsingError() {

    override val humanReadable = "Scope method is invalid: ${scope.qualifiedName}.${method.name}"

    override val testString = "${scope.simpleName}.${method.name}"
}

class ObjectsFieldFound(val scope: Scope, val objectClass: IrClass) : ParsingError() {

    override val humanReadable = "Objects class may not have fields: ${objectClass.qualifiedName}"

    override val testString = scope.simpleName
}

class ObjectsConstructorFound(val scope: Scope, val objectClass: IrClass) : ParsingError() {

    override val humanReadable = "Objects class may not define constructors: ${objectClass.qualifiedName}"

    override val testString = scope.simpleName
}

class VoidFactoryMethod(val objects: Objects, val method: IrMethod) : ParsingError() {

    override val humanReadable = "Objects methods must be non-void: ${objects.qualifiedName}.${method.name}"

    override val testString = "${objects.scope.simpleName}|${method.name}"
}

class NullableFactoryMethod(val objects: Objects, val method: IrMethod) : ParsingError() {

    override val humanReadable = "Factory method may not be nullable: ${objects.qualifiedName}.${method.name}"

    override val testString = "${objects.scope.simpleName}|${method.name}"
}

class NullableParameter(val scope: Scope, val owner: IrClass, val method: IrMethod, val parameter: IrParameter) : ParsingError() {

    override val humanReadable = "Parameter may not be nullable: ${parameter.name} in ${owner.qualifiedName}.${method.name}"

    override val testString = "${scope.simpleName}|${owner.simpleName}.${method.name}(${parameter.name})"
}

class InvalidFactoryMethod(val objects: Objects, val method: IrMethod) : ParsingError() {

    override val humanReadable = "Objects method is invalid: ${objects.qualifiedName}.${method.name}"

    override val testString = "${objects.scope.simpleName}|${method.name}"
}

class TypeNotSpreadable(val objects: Objects, val method: IrMethod, val type: IrType) : ParsingError() {

    override val humanReadable = "Type is not spreadable: ${type.qualifiedName} at ${objects.qualifiedName}.${method.name}"

    override val testString = "${objects.scope.simpleName}|${method.name}:${type.simpleName}"
}

class NoSuitableConstructor(val objects: Objects, val method: IrMethod, val type: IrType) : ParsingError() {

    override val humanReadable = "No suitable constructor found: ${type.qualifiedName} at ${objects.qualifiedName}.${method.name}"

    override val testString = "${objects.scope.simpleName}|${method.name}:${type.simpleName}"
}

class MissingInjectAnnotation(val objects: Objects, val method: IrMethod, val type: IrType) : ParsingError() {

    override val humanReadable = "Multiple constructors found. @Inject annotationn required: ${type.qualifiedName} at ${objects.qualifiedName}.${method.name}"

    override val testString = "${objects.scope.simpleName}|${method.name}:${type.simpleName}"
}

class NotAssignableBindsMethod(val objects: Objects, val method: IrMethod, val returnType: IrType, val parameterType: IrType) : ParsingError() {

    override val humanReadable = "Invalid binds method: ${objects.qualifiedName}.${method.name}"

    override val testString = "${objects.scope.simpleName}|${method.name}(${parameterType.simpleName}):${returnType.simpleName}"
}

class VoidDependenciesMethod(val scope: Scope, val dependenciesClass: IrClass, val method: IrMethod) : ParsingError() {

    override val humanReadable = "Dependencies method must be non-void: ${dependenciesClass.qualifiedName}.${method.name}"

    override val testString = "${scope.simpleName}|${method.name}"
}

class DependencyMethodWithParameters(val scope: Scope, val dependenciesClass: IrClass, val method: IrMethod) : ParsingError() {

    override val humanReadable = "Dependencies method must be parameterless: ${dependenciesClass.qualifiedName}.${method.name}"

    override val testString = "${scope.simpleName}|${method.name}"
}

class NullableSpreadMethod(val objects: Objects, val factoryMethod: IrMethod, val spreadClass: IrClass, val spreadMethod: IrMethod) : ParsingError() {

    override val humanReadable = "Spread methods must not be nullable: ${spreadClass.qualifiedName}.${spreadMethod.name}"

    override val testString = "${objects.scope.simpleName}|${factoryMethod.name}:${spreadClass.simpleName}.${spreadMethod.name}"
}