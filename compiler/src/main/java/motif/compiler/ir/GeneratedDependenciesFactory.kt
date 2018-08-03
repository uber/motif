package motif.compiler.ir

import motif.compiler.GENERATED_DEPENDENCIES_NAME
import motif.compiler.javax.Executable
import motif.compiler.javax.JavaxUtil
import motif.internal.Meta
import motif.compiler.ir
import motif.ir.source.dependencies.AnnotatedDependency
import motif.ir.source.dependencies.GeneratedDependencies
import motif.compiler.qualifiedName
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.MirroredTypesException

class GeneratedDependenciesFactory(override val env: ProcessingEnvironment) : JavaxUtil {

    fun create(scopeType: DeclaredType): GeneratedDependencies? {
        val scopeImplName = scopeImpl(scopeType).qualifiedName()
        val scopeImplType = findType(scopeImplName) as? DeclaredType ?: return null
        val generatedDependenciesType = scopeImplType.innerType(GENERATED_DEPENDENCIES_NAME) ?: return null
        val annotatedDependencies = generatedDependenciesType.methods().map(::annotatedDependency)
        return GeneratedDependencies(generatedDependenciesType, annotatedDependencies)
    }

    private fun annotatedDependency(executable: Executable): AnnotatedDependency {
        val metaAnnotation = executable.getAnnotation(Meta::class)
                ?: throw IllegalStateException("Not @Meta annotation found on generated Dependencies interface.")

        val consumingScopes = try {
            metaAnnotation.consumingScopes
            throw RuntimeException("Expected MirroredTypesException.")
        } catch (e: MirroredTypesException) {
            e.typeMirrors
        }.map { it.ir }

        return AnnotatedDependency(executable.returnedDependency, metaAnnotation.transitive, consumingScopes.toSet())
    }
}