package com.uber.motif.compiler.model

import com.google.auto.common.AnnotationMirrors
import com.google.auto.common.MoreElements
import com.google.common.base.Equivalence
import javax.inject.Qualifier
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType

private typealias QualifierKey = Equivalence.Wrapper<AnnotationMirror>

data class Dependency private constructor(
        val element: TypeElement,
        val qualifier: AnnotationMirror?,
        // TODO Hacky way of storing information about where a dependency is required / provided.
        var metaDesc: String) : Comparable<Dependency> {

    private val key: DependencyKey by lazy { DependencyKey.create(this) }
    private val compStr: String by lazy { element.toString() + qualifier?.toString() }

    val preferredName: String = element.simpleName.toString().decapitalize()

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

        fun providedByType(type: TypeElement): Dependency {
            return Dependency(type, null, metaDesc = "IMPLICIT")
        }

        fun requiredByParams(method: ExecutableElement): List<Dependency> {
            return method.parameters
                    .map {
                        val type: TypeElement = (it.asType() as DeclaredType).asElement() as TypeElement
                        val qualifier: AnnotationMirror? = it.qualifierAnnotation()
                        val owner: TypeElement = method.enclosingElement as TypeElement
                        val metaDesc = "REQUIRED_BY($owner.$method)"
                        Dependency(type, qualifier, metaDesc)
                    }
        }

        fun requiredByReturn(method: ExecutableElement): Dependency {
            return fromReturnType(method, provided = false)
        }

        fun providedByReturn(method: ExecutableElement): Dependency {
            return fromReturnType(method, provided = true)
        }

        private fun fromReturnType(method: ExecutableElement, provided: Boolean): Dependency {
            val returnType = (method.returnType as DeclaredType).asElement() as TypeElement
            val qualifier = method.qualifierAnnotation()
            val owner: TypeElement = method.enclosingElement as TypeElement
            val metaDescSuffix = "_BY($owner.$method)"
            return if (provided) {
                Dependency(returnType, qualifier, "PROVIDED$metaDescSuffix")
            } else {
                Dependency(returnType, qualifier, "REQUIRED$metaDescSuffix")
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

private data class DependencyKey(val className: String, val qualifierKey: QualifierKey?) {

    companion object {
        fun create(dependency: Dependency): DependencyKey {
            val qualifierKey = if (dependency.qualifier == null) {
                null
            } else {
                AnnotationMirrors.equivalence().wrap(dependency.qualifier)
            }
            return DependencyKey(
                    dependency.element.qualifiedName.toString(),
                    qualifierKey)
        }
    }
}