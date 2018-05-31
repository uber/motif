package com.uber.motif.compiler.codegen

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeSpec
import com.uber.motif.compiler.graph.ResolvedScope
import com.uber.motif.compiler.names.UniqueNameSet

class ModuleSpec(
        resolvedScope: ResolvedScope,
        daggerScope: DaggerScopeSpec,
        objectsImpl: ObjectsImplSpec) {

    private val methodNames: UniqueNameSet = UniqueNameSet()

    val className: ClassName = resolvedScope.scopeImplName.nestedClass("Module")

    private val objectsField: ObjectsFieldSpec = ObjectsFieldSpec(objectsImpl)

    private val internalQualifier = InternalQualifierSpec(resolvedScope)
    private val scopeProvider: ScopeProviderSpec = ScopeProviderSpec(resolvedScope, daggerScope, internalQualifier, methodNames.unique("scope"))
    private val constructorProviders: List<ConstructorProviderSpec> = resolvedScope.scope.constructorProviderMethods.map {
        ConstructorProviderSpec(
                resolvedScope,
                daggerScope,
                internalQualifier,
                it,
                methodNames.unique(it.providedDependency.preferredName))
    }
    private val bindsProviders: List<BindsProviderSpec> = resolvedScope.scope.bindsProviderMethods.map {
        BindsProviderSpec(
                resolvedScope,
                daggerScope,
                internalQualifier,
                it,
                methodNames.unique(it.providedDependency.preferredName))
    }
    private val basicProviders: List<BasicProviderSpec> = resolvedScope.scope.basicProviderMethods.map {
        BasicProviderSpec(
                resolvedScope,
                objectsField,
                daggerScope,
                internalQualifier,
                it,
                methodNames.unique(it.providedDependency.preferredName))
    }
    private val providers: List<ProviderSpec> = constructorProviders + bindsProviders + basicProviders

    val spec: TypeSpec = TypeSpec.classBuilder(className).apply {
        addAnnotation(dagger.Module::class.java)
        objectsField.spec?.let { addField(it) }
        addMethod(scopeProvider.spec)
        providers.forEach { addMethod(it.spec) }
    }.build()
}