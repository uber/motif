package motif.compiler.codegen

import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import motif.compiler.GENERATED_DEPENDENCIES_NAME
import motif.cache.ExtCacheScope
import motif.compiler.javax.ExecutableParam
import motif.compiler.ir
import motif.ir.graph.Graph
import motif.ir.graph.Scope
import motif.ir.source.base.Dependency
import motif.ir.source.child.ChildDeclaration
import motif.ir.source.dependencies.Dependencies
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Modifier

class ChildMethodFactory(
        env: ProcessingEnvironment,
        cacheScope: ExtCacheScope,
        private val graph: Graph) : CodegenCache(env, cacheScope) {

    fun create(
            scope: Scope,
            childDeclaration: ChildDeclaration): MethodSpec {
        val executable = childDeclaration.executable
        val childScopeImplName = scopeImpl(executable.returnDeclaredType)
        val childDependenciesName = childScopeImplName.nestedClass(GENERATED_DEPENDENCIES_NAME)

        return executable.overrideWithFinalParams()
                .addModifiers(Modifier.PUBLIC)
                .addStatement(
                        "return new \$T(\$L)",
                        childScopeImplName,
                        childDeclaration.childDependenciesImpl(scope, childDependenciesName))
                .build()
    }

    private fun ChildDeclaration.childDependenciesImpl(
            scope: Scope,
            childDependenciesName: TypeName): TypeSpec {
        val dynamicDependencies: Map<Dependency, ExecutableParam> = executable.parameterMap
        val componentMethods: Map<Dependency, MethodSpec> = scope.componentMethodSpecs
        fun MethodSpec.Builder.addReturn(dependency: Dependency): MethodSpec.Builder {
            val dynamicDependency = dynamicDependencies[dependency]
            val componentMethod = componentMethods[dependency]
            when {
                dynamicDependency != null -> addStatement("return \$N", dynamicDependency.element.simpleName)
                componentMethod != null -> addStatement("return \$N.\$N()", scope.componentFieldSpec, componentMethod)
                else -> throw IllegalStateException("Could not satisfy dependency: $dependency")
            }
            return this
        }

        val childType = executable.returnType.ir
        val childDependencies: Dependencies = graph.getDependencies(childType)
                ?: throw IllegalStateException("Could not find Dependenencies for child: $childType")

        val methods: List<MethodSpec> = childDependencies.methodSpecBuilders()
                .map { (dependency, builder) ->
                    builder
                            .addModifiers(Modifier.PUBLIC)
                            .addAnnotation(Override::class.java)
                            .addReturn(dependency)
                            .build()
                }
        return TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(childDependenciesName)
                .addMethods(methods)
                .build()
    }
}