package com.uber.motif.compiler.codegen

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import com.uber.motif.compiler.graph.ResolvedScope
import com.uber.motif.compiler.model.ParentInterface
import com.uber.motif.internal.Transitive
import javax.lang.model.element.Modifier

interface ParentInterfaceSpec {

    val className: ClassName
    val spec: TypeSpec?

    companion object {

        fun create(resolvedScope: ResolvedScope): ParentInterfaceSpec {
            return resolvedScope.scope.parentInterface?.let { parentInterface ->
                createExplicit(parentInterface)
            } ?: createGenerated(resolvedScope)
        }

        private fun createGenerated(resolvedScope: ResolvedScope): ParentInterfaceSpec {
            return object: ParentInterfaceSpec {
                private val methodSpecs: List<MethodSpec> = resolvedScope.parent.methods.map { method ->
                    MethodSpec.methodBuilder(method.name).apply {
                        returns(method.dependency.className)
                        method.dependency.qualifierSpec?.let { addAnnotation(it) }
                        if (method.isTransitive) addAnnotation(Transitive::class.java)
                        addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    }.build()
                }
                override val className: ClassName = resolvedScope.parent.className
                override val spec: TypeSpec = TypeSpec.interfaceBuilder(className).apply {
                    addModifiers(Modifier.PUBLIC)
                    addMethods(methodSpecs)
                }.build()
            }
        }

        private fun createExplicit(parentInterface: ParentInterface): ParentInterfaceSpec {
            return object: ParentInterfaceSpec {
                override val className: ClassName = parentInterface.type.className
                override val spec: TypeSpec? = null
            }
        }
    }
}