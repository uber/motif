package com.uber.motif.compiler.model

import com.uber.motif.compiler.isAbstract
import com.uber.motif.compiler.methods
import com.uber.motif.compiler.returnsVoid
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement

class ObjectsClass(
        val type: TypeElement,
        val constructorProviderMethods: List<ConstructorProviderMethod>,
        val bindsProviderMethods: List<BindsProviderMethod>,
        val basicProviderMethods: List<BasicProviderMethod>) {

    val providerMethods: List<ProviderMethod> by lazy {
        constructorProviderMethods + bindsProviderMethods + basicProviderMethods
    }

    val abstractProviderMethods: List<ProviderMethod> by lazy {
        providerMethods.filter { it.method.isAbstract }
    }

    val providedDependencies: Set<Dependency> by lazy {
        providerMethods.map{ it.providedDependency }.toSet()
    }

    val requiredDependencies: Set<Dependency> by lazy {
        providerMethods.flatMap { it.requiredDependencies }.toSet()
    }

    companion object {

        fun fromClass(env: ProcessingEnvironment, type: TypeElement): ObjectsClass {
            val constructorProviderMethods = mutableListOf<ConstructorProviderMethod>()
            val bindsProviderMethods = mutableListOf<BindsProviderMethod>()
            val basicProviderMethods = mutableListOf<BasicProviderMethod>()

            type.methods().forEach { method ->
                if (method.returnsVoid) {
                    throw RuntimeException("Provider method cannot return void: $method")
                }
                if (method.isAbstract) {
                    when (method.parameters.size) {
                        0 -> constructorProviderMethods.add(ConstructorProviderMethod.fromMethod(method))
                        1 -> bindsProviderMethods.add(BindsProviderMethod.fromMethod(env, method))
                        else -> throw RuntimeException("Abstract provider methods must have 0 or 1 parameter.")
                    }
                } else {
                    basicProviderMethods.add(BasicProviderMethod.fromMethod(method))
                }
            }

            return ObjectsClass(type, constructorProviderMethods, bindsProviderMethods, basicProviderMethods)
        }
    }
}