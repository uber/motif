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
package motif.compiler.javax

import motif.compiler.ir
import motif.ir.source.base.Dependency
import motif.compiler.qualifierAnnotation
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ExecutableType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import kotlin.reflect.KClass

class Executable(
        val owner: DeclaredType,
        val type: ExecutableType,
        val element: ExecutableElement) {

    val name: String = element.simpleName.toString()
    val isPrivate: Boolean = Modifier.PRIVATE in element.modifiers
    val isProtected: Boolean = Modifier.PROTECTED in element.modifiers
    val isPublic: Boolean = Modifier.PUBLIC in element.modifiers
    val isPackagePrivate: Boolean = !isPrivate && !isProtected && !isPublic
    val isAbstract: Boolean = Modifier.ABSTRACT in element.modifiers
    val returnType: TypeMirror = type.returnType
    val returnDeclaredType: DeclaredType
        get() = returnType as DeclaredType
    val isVoid: Boolean = returnType.kind == TypeKind.VOID

    fun hasAnnotation(annotationClass: KClass<out Annotation>): Boolean {
        return getAnnotation(annotationClass) != null
    }

    fun <T : Annotation> getAnnotation(annotationClass: KClass<T>): T? {
        return element.getAnnotation(annotationClass.java)
    }

    val parameters: List<ExecutableParam> by lazy {
        val parameters = element.parameters
        val types = type.parameterTypes
        (0 until parameters.size).map { i ->
            ExecutableParam(parameters[i], types[i])
        }
    }

    val parameterMap: Map<Dependency, ExecutableParam> by lazy {
        parameters.associateBy { it.dependency }
    }

    val returnedDependency: Dependency by lazy {
        Dependency(returnType, returnType.ir, element.qualifierAnnotation())
    }
}