package com.uber.motif.compiler.codegen

import com.squareup.javapoet.MethodSpec
import com.uber.motif.compiler.graph.ResolvedChild

class BridgeChildMethodImplSpec(
        componentFieldSpec: ComponentFieldSpec,
        child: ResolvedChild,
        bridgeSpec: BridgeSpec) : ChildMethodImplSpec {

    private val dynamicParameterNames: List<String> = child.method.method.parameters.map {
        it.simpleName.toString()
    }

    private val callString: String = dynamicParameterNames.joinToString(", ") { "\$L" }

    override val spec: MethodSpec = child.method.override()
            .addStatement(
                    "return \$T.\$N(\$N, $callString)",
                    bridgeSpec.className,
                    bridgeSpec.method.spec,
                    componentFieldSpec.spec,
                    *dynamicParameterNames.toTypedArray())
            .build()
}