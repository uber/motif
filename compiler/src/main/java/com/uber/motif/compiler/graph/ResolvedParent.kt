package com.uber.motif.compiler.graph

import com.squareup.javapoet.ClassName
import com.uber.motif.compiler.codegen.className
import com.uber.motif.compiler.innerInterfaces
import com.uber.motif.compiler.methods
import com.uber.motif.compiler.model.Dependency
import com.uber.motif.compiler.model.ParentInterface
import com.uber.motif.compiler.model.ParentInterfaceMethod
import com.uber.motif.compiler.names.Names
import com.uber.motif.compiler.names.UniqueNameSet
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement

class ResolvedParent(
        val className: ClassName,
        val methods: List<ParentInterfaceMethod>) {

    companion object {

        fun fromCalculated(
                scopeType: TypeElement,
                externalDependencies: Set<Dependency>,
                transitiveDependencies: Set<Dependency>): ResolvedParent {
            val methodNames = UniqueNameSet()
            val methods: List<ParentInterfaceMethod> = externalDependencies.map { dependency ->
                val transitive = dependency in transitiveDependencies
                ParentInterfaceMethod(methodNames.unique(dependency.preferredName), transitive, dependency)
            }
            val className = ClassNames.generatedParentInterface(scopeType)
            return ResolvedParent(className, methods)
        }

        fun fromExplicit(parentInterface: ParentInterface): ResolvedParent {
            return ResolvedParent(parentInterface.type.className, parentInterface.methods)
        }

        /**
         * Return the ParentInterface for this scope if the implementation is already generated. Otherwise, return null.
         */
        fun fromGenerated(env: ProcessingEnvironment, scopeType: TypeElement): ResolvedParent? {
            val scopeImplType = findScopeImpl(env, scopeType) ?: return null
            val parentInterfaceType: TypeElement = findParentInterface(scopeType, scopeImplType)
            val methods = parentInterfaceType.methods().map { ParentInterfaceMethod.fromMethod(it) }
            return ResolvedParent(parentInterfaceType.className, methods)
        }

        private fun findParentInterface(scopeType: TypeElement, scopeImplType: TypeElement): TypeElement {
            scopeImplType.getParentInterface()?.let { return it }
            // It's possible that the child scope defines the parent interface explicitly so check for that case as well.
            scopeType.getParentInterface()?.let { return it }

            throw RuntimeException("Could not find generated ScopeImpl.Parent class for: $scopeType")
        }

        private fun findScopeImpl(env: ProcessingEnvironment, scopeType: TypeElement): TypeElement? {
            val scopeImplName = ClassNames.scopeImpl(scopeType)
            return env.elementUtils.getTypeElement(scopeImplName.qualifiedName())
        }

        private fun TypeElement.getParentInterface(): TypeElement? {
            return innerInterfaces().find { it.simpleName.toString() == Names.PARENT_INTERFACE_NAME }
        }
    }
}