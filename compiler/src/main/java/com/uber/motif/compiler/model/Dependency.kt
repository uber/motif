package com.uber.motif.compiler.model

import com.google.auto.common.AnnotationMirrors
import com.uber.motif.compiler.AnnotationId
import com.uber.motif.compiler.TypeId
import com.uber.motif.compiler.asTypeElement
import com.uber.motif.compiler.id
import com.uber.motif.compiler.model.Dependency.Companion.qualifierAnnotation
import javax.inject.Qualifier
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ExecutableType
import javax.lang.model.type.TypeMirror

data class Dependency private constructor(
        val type: TypeMirror,
        val qualifier: AnnotationMirror?,
        // TODO Hacky way of storing information about where a dependency is required / provided.
        var metaDesc: String) : Comparable<Dependency> {

    private val key: DependencyKey by lazy { DependencyKey(type.id, qualifier?.id) }
    private val compStr: String by lazy { type.toString() + qualifier?.toString() }

    val preferredName: String = type.asTypeElement().simpleName.toString().decapitalize()
    val debugString: String by lazy {
        val qualifierPrefix = qualifier?.let { "$it " } ?: ""
        "$qualifierPrefix$type"
    }

    override fun compareTo(other: Dependency): Int {
        return compStr.compareTo(other.compStr)
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        val o = other as? Dependency ?: return false
        return key == o.key
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }

    companion object {

        fun providedByType(type: TypeMirror): Dependency {
            return Dependency(type, null, metaDesc = "IMPLICIT")
        }

        fun requiredByParams(owner: DeclaredType, method: ExecutableElement, methodType: ExecutableType): List<Dependency> {
            return fromParams(owner, method, methodType, provided = false)
        }

        fun providedByParams(owner: DeclaredType, method: ExecutableElement, methodType: ExecutableType): List<Dependency> {
            return fromParams(owner, method, methodType, provided = true)
        }

        private fun fromParams(owner: DeclaredType, method: ExecutableElement, methodType: ExecutableType, provided: Boolean): List<Dependency> {
            val parameterTypes = methodType.parameterTypes
            return method.parameters
                    .mapIndexed { i, it ->
                        fromParam(owner, method, it, parameterTypes[i], provided)
                    }
        }

        fun fromParam(
                owner: DeclaredType,
                method: ExecutableElement,
                parameter: VariableElement,
                type: TypeMirror,
                provided: Boolean): Dependency {
            val qualifier: AnnotationMirror? = parameter.qualifierAnnotation()
            val metaDescPrefix: String = if (provided) "PROVIDED" else "REQUIRED"
            val metaDesc = "${metaDescPrefix}_BY($owner.$method)"
            return Dependency(type, qualifier, metaDesc)
        }

        fun requiredByReturn(owner: DeclaredType, method: ExecutableElement, methodType: ExecutableType): Dependency {
            return fromReturnType(owner, method, methodType, provided = false)
        }

        fun providedByReturn(owner: DeclaredType, method: ExecutableElement, methodType: ExecutableType): Dependency {
            return fromReturnType(owner, method, methodType, provided = true)
        }

        private fun fromReturnType(
                owner: DeclaredType,
                method: ExecutableElement,
                methodType: ExecutableType,
                provided: Boolean): Dependency {
            val qualifier = method.qualifierAnnotation()
            val metaDescPrefix: String = if (provided) "PROVIDED" else "REQUIRED"
            return Dependency(methodType.returnType, qualifier, "${metaDescPrefix}_BY($owner.$method)")
        }

        private fun Element.qualifierAnnotation(): AnnotationMirror? {
            val qualifiers = AnnotationMirrors.getAnnotatedAnnotations(this, Qualifier::class.java)
            if (qualifiers.isEmpty()) {
                return null
            }
            if (qualifiers.size > 1) {
                throw RuntimeException("More than one qualifier found: $this")
            }
            return qualifiers.first()
        }
    }
}

private data class DependencyKey(val typeId: TypeId, val qualifierId: AnnotationId?)