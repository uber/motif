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
package motif.compiler

import com.google.auto.common.AnnotationMirrors
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.ArrayType
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ErrorType
import javax.lang.model.type.NoType
import javax.lang.model.type.PrimitiveType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import javax.lang.model.type.TypeVariable
import javax.lang.model.type.WildcardType
import javax.lang.model.util.SimpleElementVisitor8
import javax.lang.model.util.SimpleTypeVisitor8
import motif.ast.compiler.CompilerAnnotation
import motif.ast.compiler.CompilerType
import motif.models.Type

class NameScope(blacklist: Iterable<String> = emptySet()) {

  private val names = UniqueNameSet(blacklist)

  fun name(type: Type): String {
    return names.unique(
        Names.safeName(
            (type.type as CompilerType).mirror, (type.qualifier as? CompilerAnnotation)?.mirror))
  }
}

private class UniqueNameSet(blacklist: Iterable<String>) {

  private val used: MutableSet<String> = blacklist.toMutableSet()

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
    fun safeName(typeMirror: TypeMirror, annotation: AnnotationMirror?): String {
      var name = NameVisitor.visit(typeMirror)
      val annotationString = annotation?.let(this::annotationString) ?: ""
      name = "$annotationString$name".decapitalize()
      if (name in KEYWORDS) {
        name += "_"
      }
      return name
    }

    private fun annotationString(annotation: AnnotationMirror): String {
      return if (annotation.annotationType.toString() == "javax.inject.Named") {
        AnnotationMirrors.getAnnotationValue(annotation, "value").value.toString()
      } else {
        annotation.annotationType.asElement().simpleName.toString()
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
    t.asElement().getKind()
    val simpleName = t.asElement().simpleName.toString()
    val enclosingElementString =
        t.asElement()
            .enclosingElement
            .accept(
                object : SimpleElementVisitor8<String, Void?>() {

                  override fun defaultAction(e: Element?, p: Void?): String {
                    return ""
                  }

                  override fun visitType(e: TypeElement, p: Void?): String {
                    return e.simpleName.toString()
                  }
                },
                null)

    val rawString = "$enclosingElementString$simpleName"

    if (t.typeArguments.isEmpty()) {
      return rawString
    }

    val typeArgumentString = t.typeArguments.map { visit(it) }.joinToString("")

    return "$typeArgumentString$rawString"
  }

  override fun visitError(t: ErrorType, p: Void?): String {
    throw IllegalStateException(
        "Could not generate name for ErrorType: $t. Check your code for missing imports or typos.")
  }

  override fun visitArray(t: ArrayType, p: Void?): String {
    return visit(t.componentType) + "Array"
  }

  override fun visitTypeVariable(t: TypeVariable, p: Void?): String {
    return t.asElement().simpleName.toString().capitalize()
  }

  override fun visitWildcard(t: WildcardType, p: Void?): String {
    t.extendsBound?.let {
      return visit(it)
    }
    t.superBound?.let {
      return visit(it)
    }
    return ""
  }

  override fun visitNoType(t: NoType, p: Void?): String {
    return if (t.kind == TypeKind.VOID) "Void" else super.visitUnknown(t, p)
  }

  override fun defaultAction(e: TypeMirror, p: Void?): String {
    throw IllegalArgumentException("Unexpected type mirror: $e")
  }
}

private val KEYWORDS =
    setOf(
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
