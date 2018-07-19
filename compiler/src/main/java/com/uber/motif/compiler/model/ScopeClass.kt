package com.uber.motif.compiler.model

import com.uber.motif.Scope
import com.uber.motif.compiler.*
import com.uber.motif.compiler.names.Names
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeKind

class ScopeClass(
        val type: TypeElement,
        val objectsClass: ObjectsClass?,
        val parentInterface: ParentInterface?,
        val exposerMethods: List<ExposerMethod>,
        val childMethods: List<ChildMethod>) {

    val constructorProviderMethods: List<ConstructorProviderMethod> = objectsClass?.constructorProviderMethods ?: listOf()
    val bindsProviderMethods: List<BindsProviderMethod> = objectsClass?.bindsProviderMethods ?: listOf()
    val basicProviderMethods: List<BasicProviderMethod> = objectsClass?.basicProviderMethods ?: listOf()

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

        fun fromClass(env: ProcessingEnvironment, type: TypeElement): ScopeClass {
            val exposerMethods = mutableListOf<ExposerMethod>()
            val childMethods = mutableListOf<ChildMethod>()

            type.methods(env).forEach { method ->
                if (method.returnType.kind == TypeKind.VOID) {
                    throw RuntimeException("Invalid scope method: $method")
                }

                val returnType = method.returnType.asTypeElement()
                if (returnType.hasAnnotation(Scope::class)) {
                    childMethods.add(ChildMethod.fromMethod(method))
                } else {
                    exposerMethods.add(ExposerMethod.fromMethod(method))
                }
            }

            val objectsClass = type.innerClasses()
                    .find { it.simpleName.toString() == Names.OBJECTS_CLASS_NAME }
                    ?.let { ObjectsClass.fromClass(env, it) }

            val parentInterface = type.innerInterfaces()
                    .find { it.simpleName.toString() == Names.PARENT_INTERFACE_NAME }
                    ?.let { ParentInterface.create(env, it) }

            return ScopeClass(type, objectsClass, parentInterface, exposerMethods, childMethods)
        }
    }
}