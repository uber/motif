package com.uber.motif.compiler.graph

import com.squareup.javapoet.ClassName
import com.uber.motif.compiler.codegen.className
import com.uber.motif.compiler.names.Names
import javax.lang.model.element.TypeElement

fun ClassName.qualifiedName(): String {
    return "${packageName()}.${simpleName()}"
}

object ClassNames {

    fun scopeImpl(scopeType: TypeElement): ClassName {
        val simpleName = scopeType.simpleName.toString()
        return scopeType.className.peerClass("${simpleName}Impl")
    }

    fun generatedParentInterface(scopeType: TypeElement): ClassName {
        return scopeImpl(scopeType).nestedClass(Names.PARENT_INTERFACE_NAME)
    }
}