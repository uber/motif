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
package motif.compiler.ir

import motif.DoNotCache
import motif.Expose
import motif.Objects
import motif.Spread
import motif.compiler.errors.parsing.ParsingError
import motif.compiler.ir
import motif.compiler.javax.Executable
import motif.compiler.javax.JavaxUtil
import motif.ir.source.base.Dependency
import motif.ir.source.dependencies.RequiredDependencies
import motif.ir.source.dependencies.RequiredDependency
import motif.ir.source.objects.FactoryMethod
import motif.ir.source.objects.ObjectsClass
import motif.ir.source.objects.SpreadDependency
import motif.ir.source.objects.SpreadMethod
import javax.annotation.processing.ProcessingEnvironment
import javax.inject.Inject
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.type.DeclaredType

class ObjectsClassFactory(override val env: ProcessingEnvironment) : JavaxUtil {

    fun create(scopeType: DeclaredType): ObjectsClass? {
        val objectsType = scopeType.annotatedInnerType(Objects::class) ?: return null
        if (objectsType.hasFieldsRecursive()) {
            throw ParsingError(objectsType.asElement(), "@Objects-annotated class may not declare any fields.")
        }
        if (objectsType.hasNonDefaultConstructor()) {
            throw ParsingError(objectsType.asElement(), "@Objects-annotated class may not declare any non-default constructors.")
        }

        val methods = objectsType.methods()
                .onEach {
                    if (it.isVoid) throw ParsingError(it.element, "Factory method must not return void")
                    if (it.isPrivate) throw ParsingError(it.element, "Factory method cannot be private")
                }
                .map {
                    it.ensureNonNull()
                    // Order matters here.
                    val method = basic(scopeType, it)
                            ?: constructor(scopeType, it)
                            ?: binds(scopeType, it)
                            ?: throw ParsingError(it.element, "Invalid Objects method")
                    Pair(it, method)
                }
                .map { (executable, method) ->
                    val providedDependency = executable.returnedDependency
                    val isExposed = executable.hasAnnotation(Expose::class)
                    val isCached = !executable.hasAnnotation(DoNotCache::class)
                    FactoryMethod(
                            userData = executable,
                            kind = method.kind,
                            isExposed = isExposed,
                            isCached = isCached,
                            requiredDependencies = method.requiredDependencies,
                            providedDependency = providedDependency,
                            spreadDependency = spread(scopeType, executable))
                }
        return ObjectsClass(objectsType, methods)
    }

    private fun Executable.ensureNonNull() {
        element.ensureNonNull(element, "Factory methods must return non-null values.")
        parameters.forEach {
            it.element.ensureNonNull(element, "Factory method parameters must be non-null.")
        }
    }

    private fun Element.ensureNonNull(element: Element, message: String) {
        val isNullable = annotationMirrors.find {
            it.annotationType.asElement().simpleName.toString() == "Nullable"
        } != null
        if (isNullable) {
            throw ParsingError(element, message)
        }
    }

    private fun basic(scopeType: DeclaredType, executable: Executable): Method? {
        if (executable.isAbstract) {
            return null
        }

        val requiredDependencyList = executable.parameters.map {
            RequiredDependency(it.dependency, false, setOf(scopeType.ir))
        }
        val requiredDependencies = RequiredDependencies(requiredDependencyList)
        return Method(FactoryMethod.Kind.BASIC, requiredDependencies)
    }

    private fun constructor(scopeType: DeclaredType, executable: Executable): Method? {
        if (!executable.parameters.isEmpty()) {
            return null
        }

        val providedType = executable.returnType as DeclaredType
        val constructors = providedType.constructors()
                .map { Executable(providedType, it.toType(providedType), it) }

        if (constructors.isEmpty()) {
            throw ParsingError(providedType.asElement(), "Unable to find a constructor for type")
        }

        val constructor = constructors.find { it.hasAnnotation(Inject::class) } ?: constructors[0]
        constructor.ensureNonNull()

        val requiredDependencyList = constructor.parameters.map {
            RequiredDependency(it.dependency, false, setOf(scopeType.ir))
        }
        val requiredDependencies = RequiredDependencies(requiredDependencyList)
        return Method(FactoryMethod.Kind.CONSTRUCTOR, requiredDependencies)
    }

    private fun binds(scopeType: DeclaredType, executable: Executable): Method? {
        if (executable.parameters.size != 1) {
            return null
        }

        val parameter = executable.parameters[0]
        if (!parameter.type.isAssignableTo(executable.returnType)) {
            throw ParsingError(executable.element, "Invalid binds method. Parameter is not assignable to return type.")
        }

        val requiredDependency = RequiredDependency(parameter.dependency, false, setOf(scopeType.ir))
        val requiredDependencies = RequiredDependencies(listOf(requiredDependency))

        return Method(FactoryMethod.Kind.BINDS, requiredDependencies)
    }

    private fun spread(scopeType: DeclaredType, executable: Executable): SpreadDependency? {
        if (!executable.hasAnnotation(Spread::class)) {
            return null
        }

        val providedType = executable.returnType as DeclaredType
        val methods = providedType.methods()
                .filter { !it.isVoid && it.isPublic  && it.parameters.isEmpty()}
                .map {
                    val source = RequiredDependency(executable.returnedDependency, false, setOf(scopeType.ir))
                    SpreadMethod(it, source, it.returnedDependency)
                }

        return SpreadDependency(methods)
    }

    private data class Method(val kind: FactoryMethod.Kind, val requiredDependencies: RequiredDependencies)
}