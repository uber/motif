package motif.compiler.model

import motif.compiler.isAbstract
import motif.compiler.methods
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.type.DeclaredType

class ObjectsClass(
        val type: DeclaredType,
        val providerMethods: List<ProviderMethod>) {

    val basicProviderMethods: List<ProviderMethod> by lazy {
        providerMethods.filter { it.type == ProviderMethodType.BASIC }
    }

    val bindsProviderMethods: List<ProviderMethod> by lazy {
        providerMethods.filter { it.type == ProviderMethodType.BINDS }
    }

    val constructorProviderMethods: List<ProviderMethod> by lazy {
        providerMethods.filter { it.type == ProviderMethodType.CONSTRUCTOR }
    }

    val abstractProviderMethods: List<ProviderMethod> by lazy {
        providerMethods.filter { it.method.isAbstract }
    }

    val providedDependencies: Set<Dependency> by lazy {
        providerMethods.flatMap { it.providedDependencies }.toSet()
    }

    val requiredDependencies: Set<Dependency> by lazy {
        providerMethods.flatMap { it.requiredDependencies }.toSet()
    }

    companion object {

        fun fromClass(env: ProcessingEnvironment, type: DeclaredType): ObjectsClass {
            val methods = type.methods(env).map { ProviderMethod.create(env, type, it) }
            return ObjectsClass(type, methods)
        }
    }
}