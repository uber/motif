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

fun TypeMirror.asTypeElement(): TypeElement {
    return (this as DeclaredType).asElement() as TypeElement
}

fun TypeElement.methods(env: ProcessingEnvironment): List<ExecutableElement> {
    val objectMethods = ObjectMethods.get(env)
    val allMethods = MoreElements.getLocalAndInheritedMethods(this, env.typeUtils, env.elementUtils).asList()
    return allMethods - objectMethods
}

fun TypeElement.constructors(): List<ExecutableElement> {
    return ElementFilter.constructorsIn(enclosedElements)
}

fun TypeElement.innerClasses(): List<TypeElement> {
    return enclosedElements.filter { it.kind == ElementKind.CLASS }.map { it as TypeElement }
}

fun TypeElement.innerInterfaces(): List<TypeElement> {
    return enclosedElements.filter { it.kind == ElementKind.INTERFACE }.map { it as TypeElement }
}

val Element.isAbstract: Boolean
    get() = Modifier.ABSTRACT in modifiers

val Element.isPublic: Boolean
    get() = Modifier.PUBLIC in modifiers

val ExecutableElement.returnsVoid: Boolean
    get() = returnType.kind == TypeKind.VOID

fun Element.hasAnnotation(annotationClass: KClass<out Annotation>): Boolean {
    return getAnnotation(annotationClass.java) != null
}