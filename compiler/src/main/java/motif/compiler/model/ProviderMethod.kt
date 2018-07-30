package motif.compiler.model

import motif.DoNotCache
import motif.Spread
import motif.compiler.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ExecutableElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ExecutableType

class ProviderMethod(
        override val env: ProcessingEnvironment, // Hack until https://github.com/square/javapoet/issues/656 is resolved
        override val owner: DeclaredType,
        override val method: ExecutableElement,
        override val methodType: ExecutableType,
        val providedDependency: Dependency,
        val spreadDependencies: Map<Dependency, ExecutableElement>,
        val requiredDependencies: List<Dependency>,
        val cache: Boolean,
        val type: ProviderMethodType) : Method {

    val providedDependencies: List<Dependency> = spreadDependencies.keys.toList() + providedDependency

    companion object {

        fun create(
                env: ProcessingEnvironment,
                owner: DeclaredType,
                method: ExecutableElement): ProviderMethod {
            val type = type(method)
            val methodType: ExecutableType = owner.methodType(env, method)
            val dependencies: Dependencies = when (type) {
                ProviderMethodType.BASIC -> basic(env, owner, method, methodType)
                ProviderMethodType.BINDS -> binds(env, owner, method, methodType)
                ProviderMethodType.CONSTRUCTOR -> constructor(env, owner, method, methodType)
            }
            return ProviderMethod(
                    env,
                    owner,
                    method,
                    methodType,
                    dependencies.providedDependency,
                    dependencies.spreadDependencies,
                    dependencies.requiredDependencies,
                    !method.hasAnnotation(DoNotCache::class),
                    type)
        }

        private fun type(method: ExecutableElement): ProviderMethodType {
            if (method.returnsVoid) {
                throw RuntimeException("Provider method cannot return void: $method")
            }
            return if (method.isAbstract) {
                when (method.parameters.size) {
                    0 -> ProviderMethodType.CONSTRUCTOR
                    1 -> ProviderMethodType.BINDS
                    else -> throw RuntimeException("Abstract provider methods must have 0 or 1 parameter.")
                }
            } else {
                ProviderMethodType.BASIC
            }
        }

        private fun basic(
                env: ProcessingEnvironment,
                owner: DeclaredType,
                method: ExecutableElement,
                methodType: ExecutableType): Dependencies {
            val provided = Dependency.providedByReturn(owner, method, methodType)
            val spread = spreadDependencies(env, method, methodType)
            val required = Dependency.requiredByParams(owner, method, methodType)
            return Dependencies(provided, spread, required)
        }

        private fun binds(
                env: ProcessingEnvironment,
                owner: DeclaredType,
                method: ExecutableElement,
                methodType: ExecutableType): Dependencies {
            val provided = Dependency.providedByReturn(owner, method, methodType)
            val required = Dependency.requiredByParams(owner, method, methodType)[0]
            if (!env.typeUtils.isAssignable(required.type, provided.type)) {
                throw RuntimeException("Type ${required.type} bound by $method is not assignable to ${provided.type}")
            }
            val spread = spreadDependencies(env, method, methodType)
            return Dependencies(provided, spread, listOf(required))
        }

        private fun constructor(
                env: ProcessingEnvironment,
                owner: DeclaredType,
                method: ExecutableElement,
                methodType: ExecutableType): Dependencies {
            val provided = Dependency.providedByReturn(owner, method, methodType)
            val providedType = provided.type as DeclaredType
            // TODO Handle this better. Require @Inject if multiple constructor exist? Require @Inject always?
            val constructor = providedType.constructors()[0]
            val constructorType = providedType.methodType(env, constructor)

            val required = Dependency.requiredByParams(owner, constructor, constructorType)
            val spread = spreadDependencies(env, method, methodType)
            return Dependencies(provided, spread, required)
        }

        private fun spreadDependencies(
                env: ProcessingEnvironment,
                method: ExecutableElement,
                methodType: ExecutableType): Map<Dependency, ExecutableElement> {
            if (!method.hasAnnotation(Spread::class)) return mapOf()
            return SpreadUtil.spreadMethods(env, methodType.returnType as DeclaredType)
        }

        private data class Dependencies(
                val providedDependency: Dependency,
                val spreadDependencies: Map<Dependency, ExecutableElement>,
                val requiredDependencies: List<Dependency>)
    }
}

enum class ProviderMethodType {
    BASIC, BINDS, CONSTRUCTOR
}