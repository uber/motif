package com.uber.motif.compiler.codegen

import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeName
import com.uber.motif.compiler.graph.ResolvedScope
import com.uber.motif.compiler.model.ProviderMethod

class BasicProviderSpec(
        resolvedScope: ResolvedScope,
        private val objectsField: ObjectsFieldSpec,
        daggerScope: DaggerScopeSpec,
        internalQualifier: InternalQualifierSpec,
        providerMethod: ProviderMethod,
        name: String)
    : ProviderSpec(resolvedScope, daggerScope, internalQualifier, providerMethod, name) {

    override fun returnStatement(
            dependencyName: TypeName,
            callParams: String,
            parameters: Array<ParameterSpec>): CodeBlock {
        return CodeBlock.of("return \$N.\$L($callParams)", objectsField.spec!!, providerMethod.method.simpleName, *parameters)
    }
}