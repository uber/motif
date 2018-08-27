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
package motif.models.parsing

import motif.DoNotCache
import motif.Expose
import motif.Objects
import motif.Spread
import motif.models.java.IrClass
import motif.models.java.IrMethod
import motif.models.java.IrParameter
import motif.models.java.IrType
import motif.models.parsing.errors.*
import motif.models.motif.dependencies.Dependency
import motif.models.motif.dependencies.RequiredDependencies
import motif.models.motif.dependencies.RequiredDependency
import motif.models.motif.objects.FactoryMethod
import motif.models.motif.objects.ObjectsClass
import motif.models.motif.objects.SpreadDependency
import motif.models.motif.objects.SpreadMethod
import javax.inject.Inject

class ObjectsClassParser : ParserUtil {

    fun parse(scopeClass: IrClass): ObjectsClass? {
        val objectsClass: IrClass = scopeClass.annotatedInnerClass(Objects::class) ?: return null
        if (objectsClass.fields.any { !it.isStatic() }) throw ObjectsFieldFound()
        if (objectsClass.hasNonDefaultConstructor()) throw ObjectsConstructorFound()

        val factoryMethods: List<FactoryMethod> = objectsClass.methods
                .onEach { method ->
                    if (method.isVoid()) throw VoidObjectsMethod(method)
                    if (method.isPrivate()) throw PrivateObjectsMethod(method)
                    if (method.isNullable()) throw NullableFactoryMethod(method)
                    ensureNonNullParameters(method)
                }
                .map { method ->
                    val (kind, requiredDependencies) = basic(scopeClass, method)
                            ?: constructor(scopeClass, method)
                            ?: binds(scopeClass, method)
                            ?: throw InvalidObjectsMethod(method)
                    val providedDependency: Dependency = method.returnedDependency()
                    val isExposed: Boolean = method.hasAnnotation(Expose::class)
                    val isCached: Boolean = !method.hasAnnotation(DoNotCache::class)
                    FactoryMethod(
                            method,
                            kind,
                            scopeClass.type,
                            isExposed,
                            isCached,
                            requiredDependencies,
                            providedDependency,
                            spread(scopeClass, method))
                }
        return ObjectsClass(objectsClass.type, factoryMethods)
    }

    private fun basic(scopeClass: IrClass, method: IrMethod): ParsedMethod? {
        if (method.isAbstract()) return null

        val requiredDependencies: RequiredDependencies = method.requiredDependencies(scopeClass)
        return ParsedMethod(FactoryMethod.Kind.BASIC, requiredDependencies)
    }

    private fun constructor(scopeClass: IrClass, method: IrMethod): ParsedMethod? {
        if (method.hasParameters()) return null

        val returnType = method.returnType
        val returnClass: IrClass = returnType.resolveClass() ?: throw NoSuitableConstructor(returnType, method)

        val constructors: List<IrMethod> = returnClass.constructors

        val requiredDependencies: RequiredDependencies = if (constructors.isEmpty()) {
            RequiredDependencies(listOf())
        } else {
            val constructor = constructors.find { it.hasAnnotation(Inject::class) } ?: constructors[0]

            ensureNonNullParameters(constructor)

            constructor.requiredDependencies(scopeClass)
        }

        return ParsedMethod(FactoryMethod.Kind.CONSTRUCTOR, requiredDependencies)
    }

    private fun binds(scopeClass: IrClass, method: IrMethod): ParsedMethod? {
        if (method.parameters.size != 1) return null

        val parameter: IrParameter = method.parameters[0]
        val parameterType: IrType = parameter.type
        val returnType: IrType = method.returnType

        if (!parameterType.isAssignableTo(returnType)) {
            throw NotAssignableBindsMethod(method, returnType, parameterType)
        }

        val requiredDependency = RequiredDependency(parameter.toDependency(), false, setOf(scopeClass.type))
        val requiredDependencies = RequiredDependencies(listOf(requiredDependency))

        return ParsedMethod(FactoryMethod.Kind.BINDS, requiredDependencies)
    }

    private fun spread(scopeClass: IrClass, method: IrMethod): SpreadDependency? {
        if (!method.hasAnnotation(Spread::class)) return null

        val returnType: IrType = method.returnType
        val returnClass: IrClass = returnType.resolveClass() ?: throw TypeNotSpreadable(returnType, method)
        val methods: List<SpreadMethod> = returnClass.methods
                .filter { !it.isVoid() && it.isPublic() && !it.hasParameters() }
                .map {
                    val sourceDependency = method.returnedDependency()
                    val source = RequiredDependency(sourceDependency, false, setOf(scopeClass.type))
                    SpreadMethod(it, source, it.returnedDependency())
                }
        return SpreadDependency(methods)
    }

    private fun IrMethod.requiredDependencies(scopeClass: IrClass): RequiredDependencies {
        val requiredDependencyList: List<RequiredDependency> = parameters.map { parameter ->
            RequiredDependency(parameter.toDependency(), false, setOf(scopeClass.type))
        }
        return RequiredDependencies(requiredDependencyList)
    }

    private fun ensureNonNullParameters(method: IrMethod) {
        method.parameters.forEach { parameter ->
            if (parameter.isNullable()) throw NullableDependency(parameter, method)
        }
    }

    private data class ParsedMethod(val kind: FactoryMethod.Kind, val requiredDependencies: RequiredDependencies)
}