package motif.compiler.codegen

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeSpec
import motif.compiler.graph.ResolvedScope
import javax.lang.model.element.Modifier

class ScopeImplSpec(val resolvedScope: ResolvedScope) {

    val className: ClassName = resolvedScope.scopeImplName
    val daggerScope = DaggerScopeSpec(resolvedScope)
    val internalQualifier = InternalQualifierSpec(resolvedScope)
    val parentInterface: ParentInterfaceSpec = ParentInterfaceSpec.create(resolvedScope)
    val objectsImpl: ObjectsImplSpec = ObjectsImplSpec(resolvedScope)
    val module: ModuleSpec = ModuleSpec(resolvedScope, daggerScope, objectsImpl)
    val bridgeInfos: List<ChildBridgeInfo> = ChildBridgeInfo.fromScope(resolvedScope)
    val bridges: List<BridgeSpec> = bridgeInfos.mapNotNull { it.bridgeSpec }
    val component: ComponentSpec = ComponentSpec(resolvedScope, bridgeInfos, daggerScope, internalQualifier, parentInterface, module)
    val componentField: ComponentFieldSpec = ComponentFieldSpec(component)
    val exposerMethodImpls: List<ExposerMethodImplSpec> = resolvedScope.scope.exposerMethods.map { ExposerMethodImplSpec(component, componentField, it) }
    val childMethodImpls: List<ChildMethodImplSpec> = bridgeInfos.map { ChildMethodImplSpec.create(componentField, it) }
    val scopeConstructor: ScopeConstructorSpec = ScopeConstructorSpec(component, componentField, module, parentInterface)

    val spec: TypeSpec = TypeSpec.classBuilder(className).apply {
        addSuperinterface(resolvedScope.scopeName)
        addModifiers(Modifier.PUBLIC)
        addField(componentField.spec)
        addMethod(scopeConstructor.spec)
        scopeConstructor.altSpec?.let { addMethod(it) }
        exposerMethodImpls.forEach { addMethod(it.spec) }
        childMethodImpls.forEach { addMethod(it.spec) }
        addType(daggerScope.spec)
        addType(internalQualifier.spec)
        parentInterface.spec?.let { addType(it) }
        addType(component.spec)
        objectsImpl.spec?.let { addType(it) }
        addType(module.spec)
        bridges.forEach { addType(it.spec) }
    }.build()

    override fun toString(): String {
        return "package ${resolvedScope.packageName};\n\n$spec"
    }
}