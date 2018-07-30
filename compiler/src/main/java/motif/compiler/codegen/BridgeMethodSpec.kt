package motif.compiler.codegen

import com.squareup.javapoet.MethodSpec
import motif.compiler.graph.ResolvedChild
import javax.lang.model.element.Modifier

class BridgeMethodSpec(
        child: ResolvedChild,
        parent: BridgeParentSpec) {

    val parameters = BridgeMethodParametersSpec(child, parent)
    val parentImpl = BridgeParentImplSpec(child, parent, parameters)
    val spec: MethodSpec = MethodSpec.methodBuilder("create").apply {
        addModifiers(Modifier.STATIC)
        returns(child.scopeName)
        parameters.specs.forEach { addParameter(it) }
        addStatement("return new \$T(\$L)", child.scopeImplName, parentImpl.spec)
    }.build()
}