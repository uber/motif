package motif.compiler.codegen

import motif.compiler.graph.ResolvedChild
import motif.compiler.graph.ResolvedScope
import motif.compiler.names.UniqueNameSet

class ChildBridgeInfo(
        val child: ResolvedChild,
        val bridgeSpec: BridgeSpec?) {

    companion object {

        fun fromScope(resolvedScope: ResolvedScope): List<ChildBridgeInfo> {
            val bridgeNames = UniqueNameSet()
            return resolvedScope.children.map { child ->
                if (child.method.dynamicDependencies.isEmpty()) {
                    ChildBridgeInfo(child, null)
                } else {
                    val preferredName = "${child.baseName}Bridge"
                    ChildBridgeInfo(child, BridgeSpec(resolvedScope, child, bridgeNames.unique(preferredName)))
                }
            }
        }
    }
}