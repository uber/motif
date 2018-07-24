package com.uber.motif.compiler.codegen

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeSpec
import com.uber.motif.compiler.graph.ResolvedChild
import com.uber.motif.compiler.names.Names
import javax.lang.model.element.Modifier

class BridgeParentSpec(
        child: ResolvedChild,
        bridgeName: ClassName) {

    val className: ClassName = bridgeName.nestedClass(Names.PARENT_INTERFACE_NAME)
    val provisionMethods = ProvisionMethodsSpec(child.externalDependencies) { dependency ->
        dependency.qualifierSpec?.let { addAnnotation(it) }
    }
    val spec: TypeSpec = TypeSpec.interfaceBuilder(className).apply {
        addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        provisionMethods.specs.map { addMethod(it) }
    }.build()
}