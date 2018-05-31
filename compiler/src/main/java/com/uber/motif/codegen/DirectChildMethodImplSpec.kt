package com.uber.motif.compiler.codegen

import com.squareup.javapoet.MethodSpec
import com.uber.motif.compiler.graph.ResolvedChild

class DirectChildMethodImplSpec(
        componentFieldSpec: ComponentFieldSpec,
        child: ResolvedChild) : ChildMethodImplSpec {

    override val spec: MethodSpec = MethodSpec.overriding(child.method.method)
            .addStatement("return new \$T(\$N)", child.scopeImplName, componentFieldSpec.spec)
            .build()
}