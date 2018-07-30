package motif.compiler

import com.google.auto.common.AnnotationMirrors
import com.google.auto.common.MoreElements
import com.google.auto.common.MoreTypes
import com.google.common.base.Equivalence
import org.apache.commons.codec.binary.Base32
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ExecutableType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.ElementFilter
import kotlin.reflect.KClass

typealias TypeId = Equivalence.Wrapper<TypeMirror>
typealias AnnotationId = Equivalence.Wrapper<AnnotationMirror>

val TypeMirror.id: TypeId
    get() = MoreTypes.equivalence().wrap(this)

val AnnotationMirror.id: AnnotationId
    get() = AnnotationMirrors.equivalence().wrap(this)

fun DeclaredType.methodType(env: ProcessingEnvironment, method: ExecutableElement): ExecutableType {
    return env.typeUtils.asMemberOf(this, method) as ExecutableType
}

fun DeclaredType.asTypeElement(): TypeElement {
    return asElement() as TypeElement
}

fun TypeMirror.asTypeElement(): TypeElement {
    return (this as DeclaredType).asTypeElement()
}

fun TypeElement.asDeclaredType(): DeclaredType {
    return asType() as DeclaredType
}

fun DeclaredType.methods(env: ProcessingEnvironment): List<ExecutableElement> {
    return asTypeElement().methods(env)
}

private fun TypeElement.methods(env: ProcessingEnvironment): List<ExecutableElement> {
    return MoreElements.getLocalAndInheritedMethods(this, env.typeUtils, env.elementUtils)
            .filter { it.enclosingElement.toString() != "java.lang.Object" }
}

fun DeclaredType.constructors(): List<ExecutableElement> {
    return asTypeElement().constructors()
}

private fun TypeElement.constructors(): List<ExecutableElement> {
    return ElementFilter.constructorsIn(enclosedElements)
}

fun DeclaredType.innerTypes(): List<DeclaredType> {
    return asTypeElement().enclosedElements
            .filter { it.kind == ElementKind.CLASS || it.kind == ElementKind.INTERFACE}
            .map { it.asType() as DeclaredType }
}

fun DeclaredType.innerInterfaces(): List<DeclaredType> {
    return asTypeElement().enclosedElements
            .filter { it.kind == ElementKind.INTERFACE }
            .map { it.asType() as DeclaredType }
}

val ExecutableElement.returnsVoid: Boolean
    get() = returnType.kind == TypeKind.VOID

val DeclaredType.simpleName: String
    get() = asElement().simpleName.toString()

val Element.isAbstract: Boolean
    get() = Modifier.ABSTRACT in modifiers

val Element.isPublic: Boolean
    get() = Modifier.PUBLIC in modifiers

fun Element.hasAnnotation(annotationClass: KClass<out Annotation>): Boolean {
    return getAnnotation(annotationClass.java) != null
}

abstract class CompilationError : RuntimeException() {

    abstract override val message: String
}

private val base32 = Base32()

fun AnnotationMirror.serialize(): String {
    return base32.encodeToString(toString().toByteArray()).trimEnd('=')
}