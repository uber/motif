package motif.compiler.ir

import motif.Dependencies
import motif.compiler.errors.CompilerError
import motif.compiler.javax.JavaxUtil
import motif.ir.source.dependencies.ExplicitDependencies
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.type.DeclaredType

class ExplicitDependenciesFactory(override val env: ProcessingEnvironment) : JavaxUtil {

    fun create(scopeType: DeclaredType): ExplicitDependencies? {
        val explicitDependenciesType = scopeType.annotatedInnerType(Dependencies::class) ?: return null
        val depedencies = explicitDependenciesType.methods()
                .onEach {
                    if (it.isVoid) throw CompilerError(it.element, "Dependencies methods must not return void.")
                    if (it.parameters.isNotEmpty()) throw CompilerError(it.element, "Dependencies methods must be parameterless.")
                }
                .map { it.returnedDependency }
        return ExplicitDependencies(explicitDependenciesType, depedencies)
    }
}