package com.uber.motif.compiler.codegen

import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.uber.motif.compiler.graph.ResolvedChild

class BridgeChildMethodImplSpec(
        componentFieldSpec: ComponentFieldSpec,
        child: ResolvedChild,
        bridgeSpec: BridgeSpec) : ChildMethodImplSpec {

    private val dynamicParameters: List<ParameterSpec> = child.method.method.parameters.map {
        ParameterSpec.get(it)
    }

    private val callString: String = dynamicParameters.joinToString(", ") { "\$N" }

    override val spec: MethodSpec = MethodSpec.overriding(child.method.method)
            .addStatement(
                    "return \$T.\$N(\$N, $callString)",
                    bridgeSpec.className,
                    bridgeSpec.method.spec,
                    componentFieldSpec.spec,
                    *dynamicParameters.toTypedArray())
            .build()
}