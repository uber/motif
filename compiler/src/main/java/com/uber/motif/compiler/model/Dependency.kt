package com.uber.motif.compiler.model

import com.google.auto.common.AnnotationMirrors
import com.google.auto.common.MoreTypes
import com.google.common.base.Equivalence
import com.uber.motif.compiler.asTypeElement
import javax.inject.Qualifier
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ExecutableType
import javax.lang.model.type.TypeMirror


private typealias TypeKey = Equivalence.Wrapper<TypeMirror>
private typealias QualifierKey = Equivalence.Wrapper<AnnotationMirror>

data class Dependency private constructor(
        val type: TypeMirror,
        val qualifier: AnnotationMirror?,
        // TODO Hacky way of storing information about where a dependency is required / provided.
        var metaDesc: String) : Comparable<Dependency> {

    private val key: DependencyKey by lazy { DependencyKey.create(this) }
    private val compStr: String by lazy { type.toString() + qualifier?.toString() }

    val preferredName: String = type.asTypeElement().simpleName.toString().decapitalize()

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
            val parameterTypes = methodType.parameterTypes
            return method.parameters
                    .mapIndexed { i, it ->
                        val qualifier: AnnotationMirror? = it.qualifierAnnotation()
                        val metaDesc = "REQUIRED_BY($owner.$method)"
                        val type = parameterTypes[i]
                        Dependency(type, qualifier, metaDesc)
                    }
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
            val metaDescSuffix = "_BY($owner.$method)"
            return if (provided) {
                Dependency(methodType.returnType, qualifier, "PROVIDED$metaDescSuffix")
            } else {
                Dependency(methodType.returnType, qualifier, "REQUIRED$metaDescSuffix")
            }
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

private data class DependencyKey(val typeKey: TypeKey, val qualifierKey: QualifierKey?) {

    companion object {
        fun create(dependency: Dependency): DependencyKey {
            val qualifierKey = if (dependency.qualifier == null) {
                null
            } else {
                AnnotationMirrors.equivalence().wrap(dependency.qualifier)
            }
            val typeKey = MoreTypes.equivalence().wrap(dependency.type)
            return DependencyKey(
                    typeKey,
                    qualifierKey)
        }
    }
}