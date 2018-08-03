package motif.compiler.javax

import motif.compiler.ir
import motif.ir.source.base.Dependency
import motif.compiler.qualifierAnnotation
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror

class ExecutableParam(
        val element: VariableElement,
        val type: TypeMirror) {

    val dependency: Dependency by lazy {
        Dependency(type, type.ir, element.qualifierAnnotation())
    }
}