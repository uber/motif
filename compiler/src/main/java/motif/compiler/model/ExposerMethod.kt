package motif.compiler.model

import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ExecutableElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ExecutableType

class ExposerMethod(
        override val env: ProcessingEnvironment, // Hack until https://github.com/square/javapoet/issues/656 is resolved
        override val owner: DeclaredType,
        override val method: ExecutableElement,
        override val methodType: ExecutableType,
        val dependency: Dependency) : Method {

    companion object {

        fun fromMethod(
                env: ProcessingEnvironment,
                owner: DeclaredType,
                method: ExecutableElement,
                methodType: ExecutableType): ExposerMethod {
            if (method.parameters.isNotEmpty()) {
                throw RuntimeException("Exposer method cannot take any parameters: $method")
            }
            val dependency = Dependency.requiredByReturn(owner, method, methodType)
            return ExposerMethod(env, owner, method, methodType, dependency)
        }
    }
}