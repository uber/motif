package motif.compiler.codegen

import com.squareup.javapoet.MethodSpec

interface ChildMethodImplSpec {

    val spec: MethodSpec

    companion object {

        fun create(
                componentFieldSpec: ComponentFieldSpec,
                bridgeInfo: ChildBridgeInfo): ChildMethodImplSpec {
            return bridgeInfo.bridgeSpec?.let { bridgeSpec ->
                BridgeChildMethodImplSpec(componentFieldSpec, bridgeInfo.child, bridgeSpec)
            } ?: DirectChildMethodImplSpec(componentFieldSpec, bridgeInfo.child)
        }
    }
}