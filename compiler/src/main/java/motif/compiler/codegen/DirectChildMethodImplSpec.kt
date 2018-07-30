package motif.compiler.codegen

import com.squareup.javapoet.MethodSpec
import motif.compiler.graph.ResolvedChild

class DirectChildMethodImplSpec(
        componentFieldSpec: ComponentFieldSpec,
        child: ResolvedChild) : ChildMethodImplSpec {

    override val spec: MethodSpec = child.method.override()
            .addStatement("return new \$T(\$N)", child.scopeImplName, componentFieldSpec.spec)
            .build()
}