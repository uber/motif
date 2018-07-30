package motif.compiler.graph

import com.squareup.javapoet.ClassName
import motif.compiler.codegen.className
import motif.compiler.model.ChildMethod
import motif.compiler.model.Dependency
import motif.compiler.simpleName

class ResolvedChild(
        val method: ChildMethod,
        val resolvedParent: ResolvedParent) {

    val externalDependencies: List<Dependency> = resolvedParent.methods.map { it.dependency } - method.dynamicDependencies
    val scopeName: ClassName = method.scopeType.className
    val scopeImplName: ClassName = ClassNames.scopeImpl(method.scopeType)
    val baseName: String = method.scopeType.simpleName.removeSuffix("Scope")
}