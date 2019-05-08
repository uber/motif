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
package motif.compiler

import com.google.auto.common.AnnotationMirrors
import com.squareup.javapoet.*
import motif.ast.IrClass
import motif.ast.IrMethod
import motif.ast.IrType
import motif.ast.compiler.CompilerAnnotation
import motif.ast.compiler.CompilerMethod
import motif.ast.compiler.CompilerType
import motif.models.Type
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.*
import javax.lang.model.util.SimpleElementVisitor8
import javax.lang.model.util.SimpleTypeVisitor8

fun overrideSpec(env: ProcessingEnvironment, method: IrMethod): MethodSpec.Builder {
    val compilerMethod = method as CompilerMethod
    return MethodSpec.overriding(compilerMethod.element, compilerMethod.owner, env.typeUtils)
}

fun overrideWithFinalParamsSpec(method: IrMethod): MethodSpec.Builder {
    val builder = MethodSpec.methodBuilder(method.name)
            .addAnnotation(Override::class.java)
            .returns(method.returnType.typeName)

    method.parameters
            .map {
                ParameterSpec.builder(it.type.typeName, it.name)
                        .addModifiers(Modifier.FINAL)
                        .build()
            }
            .forEach { builder.addParameter(it) }

    return builder
}

fun methodSpec(nameScope: NameScope, type: Type): MethodSpec.Builder {
    val name = nameScope.name(type)
    val spec = MethodSpec.methodBuilder(name)
    spec.returns(type.typeName)
    type.qualifierSpec?.let { spec.addAnnotation(it) }
    return spec
}

fun parameterSpec(nameScope: NameScope, type: Type): ParameterSpec {
    val name = nameScope.name(type)
    return parameterSpec(type, name)
}

fun parameterSpec(type: Type, name: String): ParameterSpec {
    val spec = ParameterSpec.builder(type.typeName, name)
    type.qualifierSpec?.let { spec.addAnnotation(it) }
    return spec.build()
}

val IrClass.typeName: ClassName
    get() = type.typeName

val IrType.typeName: ClassName
    get() = ClassName.get((this as CompilerType).mirror) as ClassName

val Type.mirror: TypeMirror
    get() = (type as CompilerType).mirror

val Type.qualifierMirror: AnnotationMirror?
    get() = (qualifier as? CompilerAnnotation)?.mirror

val Type.typeName: TypeName
    get() = ClassName.get(mirror)

val Type.qualifierSpec: AnnotationSpec?
    get() = qualifierMirror?.let { AnnotationSpec.get(it) }

class NameScope {

    private val names = UniqueNameSet()

    fun name(type: Type): String {
        return names.unique(Names.safeName(type.mirror, type.qualifierMirror))
    }
}

private class UniqueNameSet {

    private val used: MutableSet<String> = mutableSetOf()

    fun unique(base: String): String {
        var name = base
        var i = 2
        while (!used.add(name)) {
            name = "$base${i++}"
        }
        return name
    }
}


class Names {

    companion object {

        @JvmStatic
        fun safeName(typeMirror: TypeMirror, annnotation: AnnotationMirror?): String {
            var name = NameVisitor.visit(typeMirror)
            val annotationString = annnotation?.let(this::annotationString) ?: ""
            name = "$annotationString$name".decapitalize()
            if (name in KEYWORDS) {
                name += "_"
            }
            return name
        }

        private fun annotationString(annnotation: AnnotationMirror): String {
            return if (annnotation.annotationType.toString() == "javax.inject.Named") {
                AnnotationMirrors.getAnnotationValue(annnotation, "value").value.toString()
            } else {
                annnotation.annotationType.asElement().simpleName.toString()
            }
        }
    }
}

private object NameVisitor : SimpleTypeVisitor8<String, Void>() {

    override fun visitPrimitive(t: PrimitiveType, p: Void?): String {
        return when (t.kind) {
            TypeKind.BOOLEAN -> "Boolean"
            TypeKind.BYTE -> "Byte"
            TypeKind.SHORT -> "Short"
            TypeKind.INT -> "Int"
            TypeKind.LONG -> "Long"
            TypeKind.CHAR -> "Char"
            TypeKind.FLOAT -> "Float"
            TypeKind.DOUBLE -> "Double"
            else -> throw IllegalStateException()
        }
    }

    override fun visitDeclared(t: DeclaredType, p: Void?): String {
        val simpleName = t.asElement().simpleName.toString()
        val enclosingElementString = t.asElement().enclosingElement.accept(object : SimpleElementVisitor8<String, Void?>() {

            override fun defaultAction(e: Element?, p: Void?): String {
                return ""
            }

            override fun visitType(e: TypeElement, p: Void?): String {
                return e.simpleName.toString()
            }
        }, null)

        val rawString = "$enclosingElementString$simpleName"

        if (t.typeArguments.isEmpty()) {
            return rawString
        }

        val typeArgumentString = t.typeArguments
                .map { visit(it) }
                .joinToString("")

        return "$typeArgumentString$rawString"
    }

    override fun visitError(t: ErrorType, p: Void?): String {
        return visitDeclared(t, p)
    }

    override fun visitArray(t: ArrayType, p: Void?): String {
        return visit(t.componentType) + "Array"
    }

    override fun visitTypeVariable(t: TypeVariable, p: Void?): String {
        return t.asElement().simpleName.toString().capitalize()
    }

    override fun visitWildcard(t: WildcardType, p: Void?): String {
        t.extendsBound?.let { return visit(it) }
        t.superBound?.let { return visit(it) }
        return ""
    }

    override fun visitNoType(t: NoType, p: Void?): String {
        return if (t.kind == TypeKind.VOID) "Void" else super.visitUnknown(t, p)
    }

    override fun defaultAction(e: TypeMirror, p: Void?): String {
        throw IllegalArgumentException("Unexpected type mirror: $e")
    }
}

private val KEYWORDS = setOf(
        "abstract",
        "continue",
        "for",
        "new",
        "switch",
        "assert",
        "default",
        "goto",
        "package",
        "synchronized",
        "boolean",
        "do",
        "if",
        "private",
        "this",
        "break",
        "double",
        "implements",
        "protected",
        "throw",
        "byte",
        "else",
        "import",
        "public",
        "throws",
        "case",
        "enum",
        "instanceof",
        "return",
        "transient",
        "catch",
        "extends",
        "int",
        "short",
        "try",
        "char",
        "final",
        "interface",
        "static",
        "void",
        "class",
        "finally",
        "long",
        "strictfp",
        "volatile",
        "const",
        "float",
        "native",
        "super",
        "while")