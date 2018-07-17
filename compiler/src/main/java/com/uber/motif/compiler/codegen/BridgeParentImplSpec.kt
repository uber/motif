package com.uber.motif.compiler.codegen

import com.squareup.javapoet.TypeSpec
import com.uber.motif.compiler.graph.ResolvedChild

class BridgeParentImplSpec(
        child: ResolvedChild,
        parent: BridgeParentSpec,
        bridgeParameters: BridgeMethodParametersSpec) {

    val methods: List<BridgeParentImplMethodSpec> = child.resolvedParent.methods.map {
        BridgeParentImplMethodSpec(parent, bridgeParameters, it)
    }

    val spec: TypeSpec = TypeSpec.anonymousClassBuilder("").apply {
        superclass(child.resolvedParent.className)
        methods.forEach { addMethod(it.spec) }
    }.build()
}