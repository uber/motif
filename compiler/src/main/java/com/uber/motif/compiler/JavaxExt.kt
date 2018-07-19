package com.uber.motif.compiler

import com.google.auto.common.MoreElements
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.ElementFilter
import kotlin.reflect.KClass

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

fun TypeElement.methods(env: ProcessingEnvironment): List<ExecutableElement> {
    val objectMethods = ObjectMethods.get(env)
    val allMethods = MoreElements.getLocalAndInheritedMethods(this, env.typeUtils, env.elementUtils).asList()
    return allMethods - objectMethods
}

fun TypeElement.isObject(): Boolean {
    return qualifiedName.toString() == "java.lang.Object"
}

fun TypeElement.constructors(): List<ExecutableElement> {
    return ElementFilter.constructorsIn(enclosedElements)
}

fun DeclaredType.constructors(): List<ExecutableElement> {
    return asTypeElement().constructors()
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

val DeclaredType.simpleName: String
    get() = asElement().simpleName.toString()

val Element.isAbstract: Boolean
    get() = Modifier.ABSTRACT in modifiers

val Element.isPublic: Boolean
    get() = Modifier.PUBLIC in modifiers

val ExecutableElement.returnsVoid: Boolean
    get() = returnType.kind == TypeKind.VOID

fun Element.hasAnnotation(annotationClass: KClass<out Annotation>): Boolean {
    return getAnnotation(annotationClass.java) != null
}