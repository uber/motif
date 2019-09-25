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

import com.squareup.kotlinpoet.*
import motif.internal.None

object KotlinCodeGenerator {

    fun generate(scopeImpl: ScopeImpl): FileSpec {
        val typeSpec: TypeSpec = scopeImpl.spec()
        return FileSpec.get(scopeImpl.className.kt.packageName, typeSpec)
    }

    private fun ScopeImpl.spec(): TypeSpec {
        return TypeSpec.classBuilder(className.kt).apply {
            addAnnotation(scopeImplAnnotation.spec())
            addModifiers(KModifier.PUBLIC)
            addSuperinterface(superClassName.kt)
            objectsField?.let { addProperty(it.spec()) }
            addProperty(dependenciesField.spec())
            cacheFields.forEach { addProperty(it.spec()) }
            primaryConstructor(constructor.spec())
            alternateConstructor?.let { addFunction(it.spec()) }
            accessMethodImpls.forEach { addFunction(it.spec()) }
            childMethodImpls.forEach { addFunction(it.spec()) }
            addFunction(scopeProviderMethod.spec())
            factoryProviderMethods.forEach { addFunctions(it.specs()) }
            dependencyProviderMethods.forEach { addFunction(it.spec()) }
            dependencies?.let { addType(it.spec()) }
            objectsImpl?.let { addType(it.spec()) }
        }.build()
    }

    private fun ScopeImplAnnotation.spec(): AnnotationSpec {
        return AnnotationSpec.builder(motif.ScopeImpl::class).apply {
            addMember(CodeBlock.builder().apply {
                add("children = [")
                children.forEachIndexed { i, child ->
                    val prefix = if (i == 0) "" else ", "
                    add("%L%T::class", prefix, child.kt)
                }
                add("]")
            }.build())
            addMember("scope = %T::class", scopeClassName.kt)
            addMember("dependencies = %T::class", dependenciesClassName.kt)
        }.build()
    }

    private fun ObjectsField.spec(): PropertySpec {
        return PropertySpec.builder(name, objectsClassName.kt, KModifier.PRIVATE, KModifier.FINAL)
                .initializer("%T()", objectsImplClassName.kt)
                .build()
    }

    private fun DependenciesField.spec(): PropertySpec {
        return PropertySpec.builder(name, dependenciesClassName.kt, KModifier.PRIVATE, KModifier.FINAL).build()
    }

    private fun CacheField.spec(): PropertySpec {
        return PropertySpec.builder(name, Any::class, KModifier.PRIVATE)
                .mutable(true)
                .addAnnotation(Volatile::class)
                .initializer("%T.NONE", None::class)
                .build()
    }

    private fun Constructor.spec(): FunSpec {
        return FunSpec.constructorBuilder()
                .addModifiers(KModifier.PUBLIC)
                .addParameter(dependenciesParameterName, dependenciesClassName.kt)
                .addStatement("this.%N = %N", dependenciesFieldName, dependenciesParameterName)
                .build()
    }

    private fun AlternateConstructor.spec(): FunSpec {
        return FunSpec.constructorBuilder()
                .addModifiers(KModifier.PUBLIC)
                .callThisConstructor(CodeBlock.of("object : %T {}", dependenciesClassName.kt))
                .build()
    }

    private fun AccessMethodImpl.spec(): FunSpec {
        return KotlinTypeWorkaround.overriding(overriddenMethod.element, overriddenMethod.owner, env.typeUtils)
                .addStatement("return %N()", providerMethodName)
                .build()
    }

    private fun ChildMethodImpl.spec(): FunSpec {
        val childMethodParameters = parameters
        return FunSpec.builder(childMethodName).apply {
            addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE)
            returns(childClassName.kt)
            childMethodParameters.forEach { addParameter(it.spec()) }
            addStatement("return %T(%L)", childImplClassName.kt, childDependenciesImpl.spec())
        }.build()
    }

    private fun ChildDependenciesImpl.spec(): TypeSpec {
        return TypeSpec.anonymousClassBuilder().apply {
            addSuperinterface(childDependenciesClassName.kt)
            methods.forEach { addFunction(it.spec()) }
        }.build()
    }

    private fun ChildDependencyMethodImpl.spec(): FunSpec {
        return FunSpec.builder(name)
                .addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE)
                .returns(returnTypeName.kt)
                .addCode(returnExpression.spec())
                .build()
    }

    private fun ChildDependencyMethodImpl.ReturnExpression.spec(): CodeBlock {
        return when (this) {
            is ChildDependencyMethodImpl.ReturnExpression.Parameter -> spec()
            is ChildDependencyMethodImpl.ReturnExpression.Provider -> spec()
        }
    }

    private fun ChildDependencyMethodImpl.ReturnExpression.Parameter.spec(): CodeBlock {
        return CodeBlock.of("return %N", parameterName)
    }

    private fun ChildDependencyMethodImpl.ReturnExpression.Provider.spec(): CodeBlock {
        return CodeBlock.of("return this@%T.%N()", scopeImplName.kt, providerName)
    }

    private fun ChildMethodImplParameter.spec(): ParameterSpec {
        return ParameterSpec.builder(name, typeName.kt).build()
    }

    private fun ScopeProviderMethod.spec(): FunSpec {
        return FunSpec.builder(name)
                .returns(scopeClassName.kt)
                .addStatement("return this")
                .build()
    }

    private fun FactoryProviderMethod.specs(): List<FunSpec> {
        val primarySpec = FunSpec.builder(name)
                .returns(returnTypeName.kt)
                .addCode(body.spec())
                .build()
        val spreadSpecs = spreadProviderMethods.map { it.spec() }
        return listOf(primarySpec) + spreadSpecs
    }

    private fun FactoryProviderMethodBody.spec(): CodeBlock {
        return when (this) {
            is FactoryProviderMethodBody.Cached -> spec()
            is FactoryProviderMethodBody.Uncached -> spec()
        }
    }

    private fun FactoryProviderMethodBody.Cached.spec(): CodeBlock {
        return CodeBlock.builder()
                .beginControlFlow("if (%N == %T.NONE)", cacheFieldName, None::class)
                .beginControlFlow("synchronized (this)")
                .beginControlFlow("if (%N == %T.NONE)", cacheFieldName, None::class)
                .add("%N = %L", cacheFieldName, instantiation.spec())
                .endControlFlow()
                .endControlFlow()
                .endControlFlow()
                .add("return %N as %T", cacheFieldName, returnTypeName.kt)
                .build()
    }

    private fun FactoryProviderMethodBody.Uncached.spec(): CodeBlock {
        return CodeBlock.of("return %L", instantiation.spec())
    }

    private fun FactoryProviderInstantiation.spec(): CodeBlock {
        return when (this) {
            is FactoryProviderInstantiation.Basic -> spec()
            is FactoryProviderInstantiation.Constructor -> spec()
            is FactoryProviderInstantiation.Binds -> spec()
        }
    }

    private fun FactoryProviderInstantiation.Basic.spec(): CodeBlock {
        return if (isStatic) {
            CodeBlock.of("%T.%N%L", objectsClassName.kt, factoryMethodName, callProviders.spec())
        } else {
            CodeBlock.of("%N.%N%L", objectsFieldName, factoryMethodName, callProviders.spec())
        }
    }

    private fun FactoryProviderInstantiation.Constructor.spec(): CodeBlock {
        return CodeBlock.of("%T%L", returnTypeName.kt, callProviders.spec())
    }

    private fun FactoryProviderInstantiation.Binds.spec(): CodeBlock {
        return CodeBlock.of("%N()", providerMethodName)
    }

    private fun CallProviders.spec(): String {
        val callString = providerMethodNames.joinToString { "$it()" }
        return "($callString)"
    }

    private fun SpreadProviderMethod.spec(): FunSpec {
        return FunSpec.builder(name).apply {
            returns(returnTypeName.kt)
            if (isStatic) {
                addStatement("return %T.%N()", sourceTypeName.kt, spreadMethodName)
            } else {
                addStatement("return %N().%N()", sourceProviderMethodName, spreadMethodName)
            }
        }.build()
    }

    private fun DependencyProviderMethod.spec(): FunSpec {
        return FunSpec.builder(name)
                .returns(returnTypeName.kt)
                .addStatement("return %N.%N()", dependenciesFieldName, dependencyMethodName)
                .build()
    }

    private fun Dependencies.spec(): TypeSpec {
        return TypeSpec.interfaceBuilder(className.kt).apply {
            addModifiers(KModifier.PUBLIC)
            methods.forEach { addFunction(it.spec()) }
        }.build()
    }

    private fun DependencyMethod.spec(): FunSpec {
        return FunSpec.builder(name).apply {
            qualifier?.let { addAnnotation(it.spec()) }
            addModifiers(KModifier.PUBLIC, KModifier.ABSTRACT)
            returns(returnTypeName.kt)
            addKdoc(javaDoc.spec())
        }.build()
    }

    private fun Qualifier.spec(): AnnotationSpec {
        return AnnotationSpec.get(annotation.mirror)
    }

    private fun DependencyMethodJavaDoc.spec(): CodeBlock {
        return CodeBlock.builder().apply {
            add("\nRequested from:\n")
            requestedFrom.forEach { add(it.spec()) }
            add("\n")
        }.build()
    }

    private fun JavaDocMethodLink.spec(): CodeBlock {
        return CodeBlock.of("* [%L.%N]\n", owner, methodName)
    }

    private fun ObjectsImpl.spec(): TypeSpec {
        return TypeSpec.classBuilder(className.kt).apply {
            addModifiers(KModifier.PRIVATE)
            if (isInterface) {
                addSuperinterface(superClassName.kt)
            } else {
                superclass(superClassName.kt)
            }
            abstractMethods.forEach { addFunction(it.spec()) }
        }.build()
    }

    private fun ObjectsAbstractMethod.spec(): FunSpec {
        return KotlinTypeWorkaround.overriding(overriddenMethod.element, overriddenMethod.owner, env.typeUtils)
                .addStatement("throw %T()", UnsupportedOperationException::class)
                .build()
    }
}