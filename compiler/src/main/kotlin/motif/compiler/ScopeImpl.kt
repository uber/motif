/*
 * Copyright (c) 2018-2019 Uber Technologies, Inc.
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
package motif.compiler

import com.squareup.javapoet.*
import motif.core.ResolvedGraph
import motif.core.ScopeEdge
import motif.models.AccessMethod
import motif.models.ChildMethod
import motif.models.Scope
import motif.models.Type
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Modifier

class ChildImpl(val childEdge: ScopeEdge, val dependencies: Dependencies, val implTypeName: TypeName) {

    fun getParameter(type: Type): ChildMethod.Parameter? {
        return childEdge.method.parameters.find { parameter -> parameter.type == type }
    }
}

class ScopeImpl(
        override val spec: TypeSpec,
        override val packageName: String,
        val dependencies: Dependencies) : GeneratedClass

class ScopeImplFactory(
        private val env: ProcessingEnvironment,
        private val graph: ResolvedGraph,
        private val scope: Scope,
        private val scopeImplTypeName: ClassName,
        private val dependencies: Dependencies,
        private val childImpls: List<ChildImpl>) {

    private val scopeTypeName = scope.clazz.typeName
    private val packageName = scopeImplTypeName.packageName()

    fun create(): ScopeImpl {
        val objectsImpl = ObjectsImpl.create(env, scope, scopeImplTypeName)
        val module = Module.create(scope, objectsImpl, scopeImplTypeName)
        val component = Component.create(graph, scope, dependencies, module, childImpls, scopeTypeName, scopeImplTypeName)

        val componentField = componentField(component)

        val typeSpec = TypeSpec.classBuilder(scopeImplTypeName)
        typeSpec.addAnnotation(scopeImplAnnotation(scope, dependencies))
        typeSpec.addModifiers(Modifier.PUBLIC)

        typeSpec.addSuperinterface(scopeTypeName)
        typeSpec.addField(componentField)
        typeSpec.addMethod(constructor(componentField, dependencies, component, scopeImplTypeName))
        alternateConstructor(dependencies)?.let { typeSpec.addMethod(it) }
        typeSpec.addMethods(childMethodImpls(component, componentField))
        typeSpec.addMethods(accessMethodImpls(component, componentField))
        typeSpec.addType(dependencies.spec)
        typeSpec.addType(component.spec)
        objectsImpl?.let { typeSpec.addType(it.spec) }
        typeSpec.addType(module.spec)

        return ScopeImpl(typeSpec.build(), packageName, dependencies)
    }

    private fun scopeImplAnnotation(scope: Scope, dependencies: Dependencies): AnnotationSpec {
        val builder = AnnotationSpec.builder(motif.ScopeImpl::class.java)
        if (scope.childMethods.isEmpty()) {
            builder.addMember("children", "{}")
        } else {
            scope.childMethods
                    .forEach { childMethod ->
                        val childScopeTypeName = childMethod.childScopeClass.typeName
                        builder.addMember("children", "\$T.class", childScopeTypeName)
                    }
        }
        return builder
                .addMember("scope", "\$T.class", scopeTypeName)
                .addMember("dependencies", "\$T.class", dependencies.typeName)
                .build()
    }

    private fun componentField(component: Component): FieldSpec {
        val fieldSpec = FieldSpec.builder(component.typeName, "component", Modifier.PRIVATE, Modifier.FINAL)
        return fieldSpec.build()
    }

    private fun constructor(
            componentField: FieldSpec,
            dependencies: Dependencies,
            component: Component,
            scopeImplTypeName: ClassName): MethodSpec {
        val dependenciesParam: ParameterSpec = ParameterSpec.builder(dependencies.typeName, "dependencies").build()
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(dependenciesParam)
                .addStatement("this.\$N = \$T.builder()\n" +
                        ".\$N(\$N)\n" +
                        ".\$N(this)\n" +
                        ".\$N()",
                        componentField,
                        daggerComponentName(scopeImplTypeName, component),
                        component.builder.dependenciesMethod,
                        dependenciesParam,
                        component.builder.scopeMethod,
                        component.builder.buildMethod)
                .build()
    }

    private fun alternateConstructor(dependencies: Dependencies): MethodSpec? {
        if (!dependencies.isEmpty()) {
            return null
        }
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("this(new \$T() {})", dependencies.typeName)
                .build()
    }

    private fun accessMethodImpls(
            component: Component,
            componentField: FieldSpec): List<MethodSpec> {
        return scope.accessMethods.map { accessMethod ->
            accessMethodImpl(component, componentField, accessMethod)
        }
    }

    private fun accessMethodImpl(
            component: Component,
            componentField: FieldSpec,
            accessMethod: AccessMethod): MethodSpec {
        val componentMethod = component.getMethodSpec(accessMethod.returnType)
        val methodSpec = overrideSpec(env, accessMethod.method)
        methodSpec.addStatement("return \$N.\$N()", componentField, componentMethod)
        return methodSpec.build()
    }

    private fun childMethodImpls(
            component: Component,
            componentField: FieldSpec): List<MethodSpec> {
        return childImpls.map { childImpl -> childMethodImpl(component, componentField, childImpl) }
    }

    private fun childMethodImpl(
            component: Component,
            componentField: FieldSpec,
            childImpl: ChildImpl): MethodSpec {
        val childEdge = childImpl.childEdge
        val methodSpec = overrideWithFinalParamsSpec(childEdge.method.method)
                .addModifiers(Modifier.PUBLIC)
        val returnStatement: CodeBlock = if (childImpl.dependencies.isEmpty()) {
            CodeBlock.of("return new \$T()", childImpl.implTypeName)
        } else {
            childMethodReturnStatement(component, componentField, childImpl)
        }
        methodSpec.addStatement(returnStatement)
        return methodSpec.build()
    }

    private fun childMethodReturnStatement(
            component: Component,
            componentField: FieldSpec,
            childImpl: ChildImpl): CodeBlock {
        val childImplTypeName = childImpl.implTypeName
        val childDependencies = childImpl.dependencies
        val dependenciesTypeName = childDependencies.typeName

        val dependenciesImplSpec = TypeSpec.anonymousClassBuilder("")
        dependenciesImplSpec.addSuperinterface(dependenciesTypeName)

        val childDependencyMethodImpls = childDependencyMethodImpls(component, componentField, childImpl, childDependencies)
        dependenciesImplSpec.addMethods(childDependencyMethodImpls)

        return CodeBlock.of("return new \$T(\$L)", childImplTypeName, dependenciesImplSpec.build())
    }

    private fun childDependencyMethodImpls(
            component: Component,
            componentField: FieldSpec,
            childImpl: ChildImpl,
            childDependencies: Dependencies): List<MethodSpec> {
        return childDependencies.getMethods().map { childDependencyMethodImpl(component, componentField, childImpl, it) }
    }

    private fun childDependencyMethodImpl(
            component: Component,
            componentField: FieldSpec,
            childImpl: ChildImpl,
            method: Dependencies.Method): MethodSpec {
        val abstractMethodSpec = method.methodSpec
        val methodSpec = MethodSpec.methodBuilder(abstractMethodSpec.name)
        methodSpec.addModifiers(Modifier.PUBLIC)
        methodSpec.returns(abstractMethodSpec.returnType)
        methodSpec.addAnnotation(Override::class.java)
        val returnStatement = childDependencyMethodImplReturnStatement(component, componentField, childImpl, method)
        methodSpec.addStatement(returnStatement)
        return methodSpec.build()
    }

    private fun childDependencyMethodImplReturnStatement(
            component: Component,
            componentField: FieldSpec,
            childImpl: ChildImpl,
            method: Dependencies.Method): CodeBlock {
        val returnType = method.type
        val childMethodParameter = childImpl.getParameter(returnType)
        return if (childMethodParameter == null) {
            val componentMethodSpec = component.getMethodSpec(returnType)
            return CodeBlock.of("return \$T.this.\$N.\$N()", scopeImplTypeName, componentField, componentMethodSpec)
        } else {
            CodeBlock.of("return \$N", childMethodParameter.parameter.name)
        }
    }

    private fun daggerComponentName(scopeImplTypeName: ClassName, component: Component): ClassName {
        val simpleName = component.typeName.simpleNames().joinToString("_")
        return scopeImplTypeName.peerClass("Dagger$simpleName")
    }
}

