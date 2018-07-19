package com.uber.motif.compiler.model

import com.uber.motif.Scope
import com.uber.motif.compiler.*
import com.uber.motif.compiler.names.Names
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ExecutableType
import javax.lang.model.type.TypeKind

class ScopeClass(
        val type: DeclaredType,
        val objectsClass: ObjectsClass?,
        val parentInterface: ParentInterface?,
        val exposerMethods: List<ExposerMethod>,
        val childMethods: List<ChildMethod>) {

    val constructorProviderMethods: List<ProviderMethod> = objectsClass?.constructorProviderMethods ?: listOf()
    val bindsProviderMethods: List<ProviderMethod> = objectsClass?.bindsProviderMethods ?: listOf()
    val basicProviderMethods: List<ProviderMethod> = objectsClass?.basicProviderMethods ?: listOf()

    val exposedDependencies: Set<Dependency> = exposerMethods.map { it.dependency }.toSet()

    val providedDependencies: Set<Dependency> = (objectsClass?.providedDependencies ?: setOf()) + Dependency.providedByType(type)

    val providedPublicDependencies: Set<Dependency> by lazy {
        val objectsClass = objectsClass ?: return@lazy setOf<Dependency>()
        objectsClass.providerMethods.filter { it.method.isPublic }.map{ it.providedDependency }.toSet()
    }

    val providedPrivateDependencies: Set<Dependency> = providedDependencies - providedPublicDependencies

    val dependenciesRequiredBySelf: Set<Dependency> by lazy {
        val requiredByObjectsClass = objectsClass?.requiredDependencies ?: listOf<Dependency>()
        val requiredByExposerMethods = exposerMethods.map { it.dependency }
        (requiredByObjectsClass + requiredByExposerMethods).toSet()
    }

    companion object {

        fun fromClass(env: ProcessingEnvironment, type: DeclaredType): ScopeClass {
            val exposerMethods = mutableListOf<ExposerMethod>()
            val childMethods = mutableListOf<ChildMethod>()

            type.methods(env).forEach { method ->
                if (method.returnType.kind == TypeKind.VOID) {
                    throw RuntimeException("Invalid scope method: $method")
                }
                val methodType: ExecutableType = env.typeUtils.asMemberOf(type, method) as ExecutableType

                val returnType = methodType.returnType.asTypeElement()
                if (returnType.hasAnnotation(Scope::class)) {
                    childMethods.add(ChildMethod.fromMethod(env, type, method, methodType))
                } else {
                    exposerMethods.add(ExposerMethod.fromMethod(env, type, method, methodType))
                }
            }

            val objectsClass = type.innerClasses()
                    .find { it.simpleName == Names.OBJECTS_CLASS_NAME }
                    ?.let { ObjectsClass.fromClass(env, it) }

            val parentInterface = type.innerInterfaces()
                    .find { it.simpleName == Names.PARENT_INTERFACE_NAME }
                    ?.let { ParentInterface.create(env, it) }

            return ScopeClass(type, objectsClass, parentInterface, exposerMethods, childMethods)
        }
    }
}