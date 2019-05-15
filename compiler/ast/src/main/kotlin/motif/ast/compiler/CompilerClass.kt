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
package motif.ast.compiler

import com.google.auto.common.MoreElements
import motif.ast.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ExecutableType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.ElementFilter

class CompilerClass(
        override val env: ProcessingEnvironment,
        val declaredType: DeclaredType) : IrUtil, IrClass {

    private val typeElement: TypeElement by lazy { declaredType.asElement() as TypeElement }

    override val type: IrType by lazy { CompilerType(env, declaredType) }

    override val kind: IrClass.Kind by lazy {
        when (typeElement.kind) {
            ElementKind.CLASS -> IrClass.Kind.CLASS
            ElementKind.INTERFACE -> IrClass.Kind.INTERFACE
            else -> throw IllegalStateException()
        }
    }

    override val methods: List<IrMethod> by lazy {
        val methods: List<ExecutableElement> = MoreElements.getLocalAndInheritedMethods(typeElement, env.typeUtils, env.elementUtils)
                .filter { it.enclosingElement.toString() != "java.lang.Object" }
        val nonPrivateStaticMethods: List<ExecutableElement> = ElementFilter.methodsIn(typeElement.enclosedElements)
                .filter { Modifier.STATIC in it.modifiers && Modifier.PRIVATE !in it.modifiers }
        (methods + nonPrivateStaticMethods)
                .map { executableElement ->
                    val executableType = env.typeUtils.asMemberOf(declaredType, executableElement) as ExecutableType
                    CompilerMethod(env, declaredType, executableType, executableElement)
                }
    }

    override val constructors: List<IrMethod> by lazy {
        ElementFilter.constructorsIn(typeElement.enclosedElements)
                .map { executableElement ->
                    val executableType = env.typeUtils.asMemberOf(declaredType, executableElement) as ExecutableType
                    CompilerMethod(env, declaredType, executableType, executableElement)
                }
    }

    override val nestedClasses: List<IrClass> by lazy {
        ElementFilter.typesIn(typeElement.enclosedElements)
                .map { typeElement ->
                    CompilerClass(env, typeElement.asType() as DeclaredType)
                }
    }

    override val fields: List<IrField> by lazy { declaredType.getAllFields() }

    override val annotations: List<IrAnnotation> by lazy { typeElement.irAnnotations() }

    override val modifiers: Set<IrModifier> by lazy { typeElement.irModifiers() }

    private fun DeclaredType.getAllFields(): List<IrField> {
        val typeElement: TypeElement = asElement() as TypeElement
        var fields: List<IrField> = ElementFilter.fieldsIn(typeElement.enclosedElements)
                .map { variableElement -> CompilerField(env, variableElement) }

        val superclass: TypeMirror = typeElement.superclass
        if (superclass.kind == TypeKind.DECLARED) {
            fields += (superclass as DeclaredType).getAllFields()
        } else if (superclass.kind != TypeKind.NONE) {
            // TODO Is it possible for TypeElement.superclass to return a TypeMirror of TypeKind other than
            // DECLARED or NONE? If so, then we should not throw an Exception here and instead handle the
            // recursion properly.
            throw IllegalStateException("Unknown superclass type.")
        }

        return fields
    }
}
