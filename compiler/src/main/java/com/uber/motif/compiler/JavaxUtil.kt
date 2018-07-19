package com.uber.motif.compiler

import com.google.auto.common.AnnotationMirrors
import com.google.auto.common.MoreElements
import com.google.auto.common.MoreTypes
import com.google.common.base.Equivalence
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
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

private object ObjectMethods {

    private var objectMethods: List<ExecutableElement>? = null

    fun get(env: ProcessingEnvironment): List<ExecutableElement> {
        objectMethods?.let { return it }
        val objectType = env.elementUtils.getTypeElement(Object::class.java.name)
        val objectMethods = ElementFilter.methodsIn(objectType.enclosedElements)
        this.objectMethods = objectMethods
        return objectMethods
    }
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
    val objectMethods = ObjectMethods.get(env)
    val allMethods = MoreElements.getLocalAndInheritedMethods(this, env.typeUtils, env.elementUtils).asList()
    return allMethods - objectMethods
}

fun DeclaredType.constructors(): List<ExecutableElement> {
    return asTypeElement().constructors()
}

private fun TypeElement.constructors(): List<ExecutableElement> {
    return ElementFilter.constructorsIn(enclosedElements)
}

fun DeclaredType.innerClasses(): List<DeclaredType> {
    return asTypeElement().enclosedElements
            .filter { it.kind == ElementKind.CLASS }
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