package motif

import com.google.auto.common.AnnotationMirrors
import com.squareup.javapoet.ClassName
import motif.ir.source.base.Type
import javax.inject.Qualifier
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.lang.model.type.TypeMirror

val TypeMirror.ir: Type
    get() = Type(toString())

val AnnotationMirror.ir: motif.ir.source.base.Annotation
    get() = motif.ir.source.base.Annotation(this, toString())

fun Element.qualifierAnnotation(): motif.ir.source.base.Annotation? {
    val qualifiers = AnnotationMirrors.getAnnotatedAnnotations(this, Qualifier::class.java)
    if (qualifiers.isEmpty()) {
        return null
    }
    if (qualifiers.size > 1) {
        throw IllegalStateException("More than one qualifier found: $qualifiers.")
    }
    return qualifiers.first().ir
}

fun ClassName.qualifiedName(): String {
    return "${packageName()}.${simpleNames().joinToString(".")}"
}

val GENERATED_DEPENDENCIES_NAME = "Dependencies"