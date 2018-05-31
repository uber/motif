package com.uber.motif.compiler.model

import com.google.auto.common.AnnotationMirrors
import com.google.common.base.Equivalence
import javax.inject.Qualifier
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType

private typealias QualifierKey = Equivalence.Wrapper<AnnotationMirror>

data class Dependency(val element: TypeElement, val qualifier: AnnotationMirror?) : Comparable<Dependency> {

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

        fun fromType(type: TypeElement): Dependency {
            return Dependency(type, null)
        }

        fun fromParams(method: ExecutableElement): List<Dependency> {
            return method.parameters
                    .map {
                        val type: TypeElement = (it.asType() as DeclaredType).asElement() as TypeElement
                        val qualifier: AnnotationMirror? = it.qualifierAnnotation()
                        Dependency(type, qualifier)
                    }
        }

        fun fromReturnType(method: ExecutableElement): Dependency {
            val returnType = (method.returnType as DeclaredType).asElement() as TypeElement
            val qualifier = method.qualifierAnnotation()
            return Dependency(returnType, qualifier)
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