package motif.compiler.codegen

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeSpec
import motif.compiler.graph.ResolvedScope
import dagger.Component

class ComponentSpec(
        private val resolvedScope: ResolvedScope,
        bridgeInfos: List<ChildBridgeInfo>,
        daggerScope: DaggerScopeSpec,
        internalQualifier: InternalQualifierSpec,
        parentInterface: ParentInterfaceSpec,
        module: ModuleSpec) {

    val className: ClassName = resolvedScope.scopeImplName.nestedClass("Component")
    val daggerClassName: ClassName = daggerComponentName(className)

    val provisionMethods = ProvisionMethodsSpec(resolvedScope.scope.exposedDependencies) { dependency ->
        val isInternal = dependency in resolvedScope.scope.providedPrivateDependencies
        internalQualifier.annotationSpec(dependency, isInternal)?.let { addAnnotation(it) }
    }
    val spec: TypeSpec = TypeSpec.interfaceBuilder(className).apply {
        addAnnotation(daggerScope.className)
        addAnnotation(AnnotationSpec.builder(Component::class.java)
                .addMember("modules", "\$T.class", module.className)
                .addMember("dependencies", "\$T.class", parentInterface.className)
                .build())
        bridgeInfos.forEach {
            // For each child, extends it's parent or it's bridge's parent.
            val superInterface: ClassName = it.bridgeSpec?.parent?.className ?: it.child.resolvedParent.className
            addSuperinterface(superInterface)
        }
        provisionMethods.specs.forEach { addMethod(it) }
    }.build()

    private fun daggerComponentName(componentName: ClassName): ClassName {
        val simpleName = componentName.simpleNames().joinToString("_")
        return resolvedScope.scopeImplName.peerClass("Dagger$simpleName")
    }
}