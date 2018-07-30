package motif.compiler.model

import motif.compiler.hasAnnotation
import motif.internal.Transitive
import javax.lang.model.element.ExecutableElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ExecutableType

class ParentInterfaceMethod(
        val name: String,
        val isTransitive: Boolean,
        val dependency: Dependency) {

    companion object {

        fun fromMethod(
                owner: DeclaredType,
                method: ExecutableElement,
                methodType: ExecutableType): ParentInterfaceMethod {
            if (method.parameters.size > 0) throw RuntimeException("Parent interface method must not take any parameters: $method")
            val dependency = Dependency.requiredByReturn(owner, method, methodType)
            val methodName = method.simpleName.toString()
            val transitive = method.hasAnnotation(Transitive::class)
            return ParentInterfaceMethod(methodName, transitive, dependency)
        }
    }
}