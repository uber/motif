package motif.compiler.javax

import com.google.auto.common.MoreElements
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ExecutableType
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.ElementFilter
import kotlin.reflect.KClass

interface JavaxUtil {

    val env: ProcessingEnvironment

    fun Element.hasAnnotation(annotationClass: KClass<out Annotation>): Boolean {
        return getAnnotation(annotationClass.java) != null
    }

    fun DeclaredType.methods(): List<Executable> {
        return MoreElements.getLocalAndInheritedMethods(asElement() as TypeElement, env.typeUtils, env.elementUtils)
                .filter { it.enclosingElement.toString() != "java.lang.Object" }
                .map { executableElement ->
                    Executable(this, executableElement.toType(this), executableElement)
                }
    }

    fun DeclaredType.annotatedInnerType(annotationClass: KClass<out Annotation>): DeclaredType? {
        return asElement().enclosedElements
                .filter { it.kind == ElementKind.CLASS || it.kind == ElementKind.INTERFACE}
                .find { it.hasAnnotation(annotationClass) }?.asType() as? DeclaredType
    }

    fun DeclaredType.innerType(name: String): DeclaredType? {
        return asElement().enclosedElements
                .filter { it.kind == ElementKind.CLASS || it.kind == ElementKind.INTERFACE}
                .find { it.simpleName.toString() == name }?.asType() as? DeclaredType
    }

    fun ExecutableElement.toType(owner: DeclaredType): ExecutableType {
        return env.typeUtils.asMemberOf(owner, this) as ExecutableType
    }

    fun findType(name: String): TypeMirror? {
        return env.elementUtils.getTypeElement(name)?.asType()
    }

    fun TypeMirror.isAssignableTo(to: TypeMirror): Boolean {
        return env.typeUtils.isAssignable(this, to)
    }

    fun DeclaredType.constructors(): List<ExecutableElement> {
        return ElementFilter.constructorsIn(asElement().enclosedElements)
    }

    fun DeclaredType.packageName(): String {
        return MoreElements.getPackage(asElement()).qualifiedName.toString()
    }

    fun scopeImpl(scopeType: DeclaredType): ClassName {
        val scopeClassName = scopeType.typeName as ClassName
        val prefix = scopeClassName.simpleNames().joinToString("_")
        return ClassName.get(scopeClassName.packageName(), "${prefix}Impl")
    }

    val TypeMirror.typeName: TypeName
        get() = ClassName.get(this)
}