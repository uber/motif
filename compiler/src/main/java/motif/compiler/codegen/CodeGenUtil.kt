package motif.compiler.codegen

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import motif.compiler.asTypeElement
import motif.compiler.model.Dependency
import motif.compiler.model.Method
import motif.compiler.model.ObjectsClass
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror

val DeclaredType.className: ClassName
    get() = ClassName.get(this.asTypeElement())

val TypeMirror.typeName: TypeName
    get() = ClassName.get(this)

val TypeElement.className: ClassName
    get() = ClassName.get(this)

val ObjectsClass.className: ClassName
    get() = type.className

val Dependency.className: ClassName
    get() = type.asTypeElement().className

val Dependency.qualifierSpec: AnnotationSpec?
    get() = qualifier?.let { AnnotationSpec.get(qualifier) }

fun Method.override(): MethodSpec.Builder {
    return MethodSpec.overriding(method, owner, env.typeUtils)
}