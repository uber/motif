package com.uber.motif.compiler.model

import com.uber.motif.compiler.isPublic
import com.uber.motif.compiler.methods
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ExecutableElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ExecutableType
import javax.lang.model.type.TypeKind

object SpreadUtil {

    fun spreadMethods(env: ProcessingEnvironment, type: DeclaredType): Map<Dependency, ExecutableElement> {
        return type.methods(env)
                .filter {
                    // Only accept public non-void parameterless methods.
                    it.returnType.kind != TypeKind.VOID
                            && it.parameters.isEmpty()
                            && it.isPublic
                }.associateBy { method ->
                    val methodType = env.typeUtils.asMemberOf(type, method) as ExecutableType
                    Dependency.providedByReturn(type, method, methodType)
                }
    }
}