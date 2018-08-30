/*
 * Copyright (c) 2018 Uber Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package motif.compiler.codegen

import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import motif.compiler.ir.CompilerMethodParameter
import motif.models.graph.Graph
import motif.models.graph.Scope
import motif.models.motif.dependencies.Dependency
import motif.models.motif.child.ChildMethod
import motif.models.motif.dependencies.RequiredDependencies
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Modifier
import javax.lang.model.type.DeclaredType

class ChildMethodFactory(
        env: ProcessingEnvironment,
        cacheScope: CacheScope,
        private val graph: Graph) : CodegenCache(env, cacheScope) {

    fun create(
            scope: Scope,
            childMethod: ChildMethod): MethodSpec {
        val executable = childMethod.cir
        val childScopeImplName = scopeImpl(executable.returnType.cir.mirror as DeclaredType)
        val childDependenciesName = childScopeImplName.nestedClass(GENERATED_DEPENDENCIES_NAME)

        return executable.overrideWithFinalParams()
                .addModifiers(Modifier.PUBLIC)
                .addStatement(
                        "return new \$T(\$L)",
                        childScopeImplName,
                        childMethod.childDependenciesImpl(scope, childDependenciesName))
                .build()
    }

    private fun ChildMethod.childDependenciesImpl(
            scope: Scope,
            childDependenciesName: TypeName): TypeSpec {
        val dynamicDependencies: Map<Dependency, CompilerMethodParameter> = cir.parameters.associateBy({ parameter ->
            parameter.toDependency()
        }) { it }
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

        val childType = cir.returnType
        val childRequiredDependencies: RequiredDependencies = graph.getDependencies(childType)
                ?: throw IllegalStateException("Could not find Dependenencies for child: $childType")

        val methods: List<MethodSpec> = childRequiredDependencies.methodSpecBuilders()
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