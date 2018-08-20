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

import com.google.auto.common.MoreElements
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ExecutableType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.ElementFilter
import kotlin.reflect.KClass

interface JavaxUtil {

    val env: ProcessingEnvironment

    fun Element.hasAnnotation(annotationClass: KClass<out Annotation>): Boolean {
        return getAnnotation(annotationClass.java) != null
    }

    fun DeclaredType.methods(): List<Executable> {
        return MoreElements.getLocalAndInheritedMethods(asElement() as TypeElement, env.typeUtils, env.elementUtils)
                .filter { it.enclosingElement.toString() != "java.lang.Object" }
                .map { executableElement ->
                    Executable(this, executableElement.toType(this), executableElement)
                }
    }

    fun DeclaredType.annotatedInnerType(annotationClass: KClass<out Annotation>): DeclaredType? {
        return asElement().enclosedElements
                .filter { it.kind == ElementKind.CLASS || it.kind == ElementKind.INTERFACE}
                .find { it.hasAnnotation(annotationClass) }?.asType() as? DeclaredType
    }

    fun DeclaredType.innerType(name: String): DeclaredType? {
        return asElement().enclosedElements
                .filter { it.kind == ElementKind.CLASS || it.kind == ElementKind.INTERFACE}
                .find { it.simpleName.toString() == name }?.asType() as? DeclaredType
    }

    fun ExecutableElement.toType(owner: DeclaredType): ExecutableType {
        return env.typeUtils.asMemberOf(owner, this) as ExecutableType
    }

    fun findType(name: String): TypeMirror? {
        return env.elementUtils.getTypeElement(name)?.asType()
    }

    fun TypeMirror.isAssignableTo(to: TypeMirror): Boolean {
        return env.typeUtils.isAssignable(this, to)
    }

    fun DeclaredType.constructors(): List<ExecutableElement> {
        return ElementFilter.constructorsIn(asElement().enclosedElements)
    }

    fun DeclaredType.hasFieldsRecursive(): Boolean {
        val typeElement: TypeElement = asElement() as TypeElement
        if (ElementFilter.fieldsIn(typeElement.enclosedElements).isNotEmpty()) {
            return true
        }

        val superclass: TypeMirror = typeElement.superclass
        if (superclass.kind == TypeKind.DECLARED) {
            if ((superclass as DeclaredType).hasFieldsRecursive()) {
                return true
            }
        } else if (superclass.kind != TypeKind.NONE) {
            // TODO Is it possible for TypeElement.superclass to return a TypeMirror of TypeKind other than
            // DECLARED or NONE? If so, then we should not throw an Exception here and instead handle the
            // recursion properly.
            throw IllegalStateException("Unknown superclass type.")
        }

        return false
    }

    fun DeclaredType.hasNonDefaultConstructor(): Boolean {
        return constructors().find { !it.parameters.isEmpty() } != null
    }

    fun DeclaredType.packageName(): String {
        return MoreElements.getPackage(asElement()).qualifiedName.toString()
    }

    fun scopeImpl(scopeType: DeclaredType): ClassName {
        val scopeClassName = scopeType.typeName as ClassName
        val prefix = scopeClassName.simpleNames().joinToString("_")
        return ClassName.get(scopeClassName.packageName(), "${prefix}Impl")
    }

    val TypeMirror.typeName: TypeName
        get() = ClassName.get(this)
}