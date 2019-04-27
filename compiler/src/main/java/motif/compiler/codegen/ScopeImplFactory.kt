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

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeSpec
import dagger.BindsInstance
import dagger.Component
import motif.ScopeImpl
import motif.ast.compiler.CompilerType
import motif.internal.DaggerScope
import motif.models.graph.Graph
import motif.models.graph.Scope
import motif.models.motif.accessmethod.AccessMethod
import motif.models.motif.objects.ObjectsClass
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Modifier

class ScopeImplFactory(
        env: ProcessingEnvironment,
        cacheScope: CacheScope,
        graph: Graph) : CodegenCache(env, cacheScope) {

    private val childMethodFactory = ChildMethodFactory(env, cacheScope, graph)
    private val moduleFactory = ModuleFactory(env, cacheScope)

    fun create(scope: Scope): TypeSpec {
        val childMethods = scope.childMethods.map { childMethodFactory.create(scope, it) }
        val accessMethodImpls = scope.accessMethods.map { it.implSpec(scope) }

        return TypeSpec.classBuilder(scope.implTypeName)
                .addAnnotation(scopeImplAnnotation(scope, childMethods))
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(scope.typeName)
                .addField(scope.componentFieldSpec)
                .addType(dependencies(scope))
                .addType(component(scope))
                .addObjectsImplSpec(scope)
                .addType(moduleFactory.create(scope))
                .addMethod(scope.constructorSpec())
                .addAltConstructorSpec(scope)
                .addMethods(childMethods)
                .addMethods(accessMethodImpls)
                .build()
                .write(scope.packageName)
    }

    private fun scopeImplAnnotation(scope: Scope, childMethods: List<MethodSpec>): AnnotationSpec {
        val builder = AnnotationSpec.builder(ScopeImpl::class.java)
        if (childMethods.isEmpty()) {
            builder.addMember("children", "{}")
        } else {
            childMethods
                    .map { childMethod -> childMethod.returnType }
                    .forEach { childType ->
                        builder.addMember("children", "\$T.class", childType)
                    }
        }
        return builder
                .addMember("scope", "\$T.class", scope.typeName)
                .addMember("dependencies", "\$T.class", scope.dependenciesTypeName)
                .build()
    }

    private fun component(scope: Scope): TypeSpec {
        val builder = TypeSpec.interfaceBuilder(scope.componentBuilderTypeName)
                .addAnnotation(Component.Builder::class.java)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addMethod(MethodSpec.methodBuilder("dependencies")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .returns(scope.componentBuilderTypeName)
                        .addParameter(scope.dependenciesTypeName, "dependencies")
                        .build())
                .addMethod(MethodSpec.methodBuilder("scope")
                        .addAnnotation(BindsInstance::class.java)
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .returns(scope.componentBuilderTypeName)
                        .addParameter(scope.typeName, "scope")
                        .build())
                .addMethod(MethodSpec.methodBuilder("build")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .returns(scope.componentTypeName)
                        .build())
                .build()
        return TypeSpec.interfaceBuilder(scope.componentTypeName)
                .addAnnotation(DaggerScope::class.java)
                .addAnnotation(AnnotationSpec.builder(Component::class.java)
                        .addMember("dependencies", "\$T.class", scope.dependenciesTypeName)
                        .addMember("modules", "\$T.class", scope.moduleTypeName)
                        .build())
                .addMethods(scope.componentMethodSpecs.map { it.value })
                .addType(builder)
                .build()
    }

    private fun dependencies(scope: Scope): TypeSpec {
        return TypeSpec.interfaceBuilder(scope.dependenciesTypeName)
                .addModifiers(Modifier.PUBLIC)
                .addMethods(scope.requiredDependencies.methodSpecBuilders().map {
                    it.value.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT).build()
                })
                .build()
    }

    private fun TypeSpec.Builder.addObjectsImplSpec(scope: Scope): TypeSpec.Builder {
        scope.objectsClass?.let {
            addType(it.implSpec(scope))
        }
        return this
    }

    private fun TypeSpec.Builder.addAltConstructorSpec(scope: Scope): TypeSpec.Builder {
        if (!scope.requiredDependencies.list.isEmpty()) return this

        val spec = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("this(new \$T() {})", scope.dependenciesTypeName)
                .build()
        addMethod(spec)
        return this
    }

    private fun Scope.constructorSpec(): MethodSpec {
        val dependenciesParam: ParameterSpec = ParameterSpec.builder(dependenciesTypeName, "dependencies").build()
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(dependenciesParam)
                .addStatement("this.\$N = \$T.builder()\n" +
                        ".dependencies(\$N)\n" +
                        ".scope(this)\n" +
                        ".build()",
                        componentFieldSpec,
                        daggerComponentName,
                        dependenciesParam)
                .build()
    }

    private fun ObjectsClass.implSpec(scope: Scope): TypeSpec {
        val overriddenMethods = abstractFactoryMethods().map { it.overrideUnsupported() }
        return TypeSpec.classBuilder(scope.objectsImplTypeName)
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .apply { if ((type as CompilerType).isInterface()) addSuperinterface(typeName) else superclass(typeName) }
                .addMethods(overriddenMethods)
                .build()
    }

    private fun AccessMethod.implSpec(scope: Scope): MethodSpec {
        val componentMethod = scope.componentMethodSpecs[dependency]
                ?: throw IllegalStateException("Could not find component method for AccessMethod: $dependency")
        return cir.overriding()
                .addStatement("return \$N.\$N()", scope.componentFieldSpec, componentMethod)
                .build()
    }
}