package motif.compiler.codegen

import javax.lang.model.type.*
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
        val rawString = t.asElement().simpleName.toString()

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