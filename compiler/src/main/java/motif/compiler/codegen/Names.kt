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
package motif.compiler.codegen

import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.*
import javax.lang.model.util.SimpleElementVisitor8
import javax.lang.model.util.SimpleTypeVisitor8

class Names {

    companion object {

        @JvmStatic
        fun safeName(typeMirror: TypeMirror): String {
            return NameVisitor.visit(typeMirror).decapitalize()
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