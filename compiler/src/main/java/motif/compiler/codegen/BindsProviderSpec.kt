package motif.compiler.codegen

import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeName
import motif.compiler.graph.ResolvedScope
import motif.compiler.model.ProviderMethod

class BindsProviderSpec(
        resolvedScope: ResolvedScope,
        daggerScope: DaggerScopeSpec,
        internalQualifier: InternalQualifierSpec,
        providerMethod: ProviderMethod,
        name: String)
    : ProviderSpec(resolvedScope, daggerScope, internalQualifier, providerMethod, name) {

    override fun returnStatement(
            dependencyName: TypeName,
            callParams: String,
            parameters: Array<ParameterSpec>): CodeBlock {
        return CodeBlock.of("return $callParams", *parameters)
    }
}