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

import androidx.room.compiler.processing.XProcessingEnv
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.javapoet.KotlinPoetJavaPoetPreview
import com.squareup.kotlinpoet.javapoet.toKClassName
import motif.internal.None

@OptIn(KotlinPoetJavaPoetPreview::class)
object KotlinCodeGenerator {

  fun generate(scopeImpl: ScopeImpl): FileSpec {
    val typeSpec: TypeSpec = scopeImpl.spec()
    return FileSpec.get(scopeImpl.className.kt.packageName, typeSpec)
  }

  private fun ScopeImpl.spec(): TypeSpec =
      TypeSpec.classBuilder(className.kt)
          .apply {
            addAnnotation(suppressAnnotationSpec("REDUNDANT_PROJECTION", "UNCHECKED_CAST"))
            addAnnotation(scopeImplAnnotation.spec())
            addModifiers(if (internalScope) KModifier.INTERNAL else KModifier.PUBLIC)
            addSuperinterface(superClassName.kt)
            objectsField?.let { addProperty(it.spec()) }
            addProperty(dependenciesField.spec())
            cacheFields.forEach { addProperty(it.spec(useNullFieldInitialization)) }
            primaryConstructor(constructor.spec())
            alternateConstructor?.let { addFunction(it.spec()) }
            accessMethodImpls
                .filter { !it.overriddenMethod.isSynthetic }
                .forEach { addFunction(it.spec()) }
            accessMethodImpls
                .filter { it.overriddenMethod.isSynthetic }
                .forEach { addProperty(it.propSpec()) }
            childMethodImpls.forEach { addFunction(it.spec()) }
            addFunction(scopeProviderMethod.spec())
            factoryProviderMethods.forEach { addFunctions(it.specs(useNullFieldInitialization)) }
            dependencyProviderMethods.forEach { addFunction(it.spec()) }
            dependencies?.let { addType(it.spec()) }
            objectsImpl?.let { addType(it.spec()) }
          }
          .build()

  private fun ScopeImplAnnotation.spec(): AnnotationSpec =
      AnnotationSpec.builder(motif.ScopeImpl::class)
          .apply {
            addMember(
                CodeBlock.builder()
                    .apply {
                      add("children = [")
                      children.forEachIndexed { i, child ->
                        val prefix = if (i == 0) "" else ", "
                        add("%L%T::class", prefix, child.kt)
                      }
                      add("]")
                    }
                    .build(),
            )
            addMember("scope = %T::class", scopeClassName.kt)
            addMember("dependencies = %T::class", dependenciesClassName.kt)
          }
          .build()

  private fun ObjectsField.spec(): PropertySpec =
      PropertySpec.builder(name, objectsClassName.kt, KModifier.PRIVATE, KModifier.FINAL)
          .initializer("%T()", objectsImplClassName.kt)
          .build()

  private fun DependenciesField.spec(): PropertySpec =
      PropertySpec.builder(name, dependenciesClassName.kt, KModifier.PRIVATE)
          .initializer(name)
          .build()

  private fun CacheField.spec(useNullFieldInitialization: Boolean): PropertySpec =
      if (useNullFieldInitialization) {
        PropertySpec.builder(name, Any::class.asTypeName().copy(true), KModifier.PRIVATE)
            .mutable(true)
            .addAnnotation(Volatile::class)
            .initializer("null")
            .build()
      } else {
        PropertySpec.builder(name, Any::class, KModifier.PRIVATE)
            .mutable(true)
            .addAnnotation(Volatile::class)
            .initializer("%T.NONE", None::class)
            .build()
      }

  private fun Constructor.spec(): FunSpec =
      FunSpec.constructorBuilder()
          .addParameter(dependenciesParameterName, dependenciesClassName.kt)
          .build()

  private fun AlternateConstructor.spec(): FunSpec =
      FunSpec.constructorBuilder()
          .addModifiers(KModifier.PUBLIC)
          .callThisConstructor(CodeBlock.of("object : %T {}", dependenciesClassName.kt))
          .build()

  private fun AccessMethodImpl.spec(): FunSpec =
      XFunSpec.overriding(overriddenMethod.element, overriddenMethod.owner, env)
          .addStatement("return %N()", providerMethodName)
          .build()

  private fun AccessMethodImpl.propSpec(): PropertySpec {
    val propName =
        with(overriddenMethod.name) {
          when {
            startsWith("get") -> this.substring(3).decapitalize()
            startsWith("is") -> this.substring(2).decapitalize()
            else -> this
          }
        }
    return PropertySpec.builder(
            propName,
            ClassName.bestGuess(overriddenMethod.returnType.qualifiedName),
        )
        .addModifiers(KModifier.OVERRIDE)
        .initializer("%N()", providerMethodName)
        .build()
  }

  private fun ChildMethodImpl.spec(): FunSpec {
    val childMethodParameters = parameters
    return FunSpec.builder(childMethodName)
        .apply {
          addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE)
          returns(childClassName.kt)
          childMethodParameters.forEach { addParameter(it.spec()) }
          addStatement("return %T(%L)", childImplClassName.kt, childDependenciesImpl.spec())
        }
        .build()
  }

  private fun ChildDependenciesImpl.spec(): TypeSpec =
      TypeSpec.anonymousClassBuilder()
          .apply {
            if (isAbstractClass) {
              superclass(childDependenciesClassName.kt)
            } else {
              addSuperinterface(childDependenciesClassName.kt)
            }
            methods.forEach { addFunction(it.spec()) }
          }
          .build()

  private fun ChildDependencyMethodImpl.spec(): FunSpec =
      FunSpec.builder(name)
          .addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE)
          .returns(returnTypeName.kt)
          .addCode(returnExpression.spec())
          .build()

  private fun ChildDependencyMethodImpl.ReturnExpression.spec(): CodeBlock =
      when (this) {
        is ChildDependencyMethodImpl.ReturnExpression.Parameter -> spec()
        is ChildDependencyMethodImpl.ReturnExpression.Provider -> spec()
      }

  private fun ChildDependencyMethodImpl.ReturnExpression.Parameter.spec(): CodeBlock =
      CodeBlock.of("return %N", parameterName)

  private fun ChildDependencyMethodImpl.ReturnExpression.Provider.spec(): CodeBlock =
      CodeBlock.of("return this@%T.%N()", scopeImplName.kt, providerName)

  private fun ChildMethodImplParameter.spec(): ParameterSpec =
      ParameterSpec.builder(name, typeName.kt).build()

  private fun ScopeProviderMethod.spec(): FunSpec =
      FunSpec.builder(name)
          .apply {
            if (isInternal) {
              addModifiers(KModifier.INTERNAL)
            }
          }
          .returns(scopeClassName.kt)
          .addStatement("return this")
          .build()

  private fun FactoryProviderMethod.specs(useNullFieldInitialization: Boolean): List<FunSpec> {
    val primarySpec =
        FunSpec.builder(name)
            .addModifiers(KModifier.INTERNAL)
            .returns(returnTypeName.reloadedForTypeArgs(env))
            .addCode(body.spec(useNullFieldInitialization))
            .build()
    val spreadSpecs = spreadProviderMethods.map { it.spec() }
    return listOf(primarySpec) + spreadSpecs
  }

  private fun FactoryProviderMethodBody.spec(useNullFieldInitialization: Boolean): CodeBlock =
      when (this) {
        is FactoryProviderMethodBody.Cached -> spec(useNullFieldInitialization)
        is FactoryProviderMethodBody.Uncached -> spec()
      }

  private fun FactoryProviderMethodBody.Cached.spec(
      useNullFieldInitialization: Boolean,
  ): CodeBlock {
    if (useNullFieldInitialization) {
      val localFieldName = "_$cacheFieldName"
      val codeBlockBuilder =
          CodeBlock.builder()
              // Using a local variable reduces atomic read overhead
              .addStatement("var $localFieldName = %N;\n", cacheFieldName)
              .beginControlFlow("if (%N == null)", localFieldName)
              .beginControlFlow("synchronized (this)")
              .addStatement("%N = %N", localFieldName, cacheFieldName)
              .beginControlFlow("if (%N == null)", localFieldName)
              .addStatement("%N = %L", localFieldName, instantiation.spec())
              .addStatement("%N = %N", cacheFieldName, localFieldName)
              .endControlFlow()
              .endControlFlow()
              .endControlFlow()
      return codeBlockBuilder
          .add("return ( %N as %T )", localFieldName, returnTypeName.reloadedForTypeArgs(env))
          .build()
    }
    return CodeBlock.builder()
        .beginControlFlow("if (%N == %T.NONE)", cacheFieldName, None::class)
        .beginControlFlow("synchronized (this)")
        .beginControlFlow("if (%N == %T.NONE)", cacheFieldName, None::class)
        .addStatement("%N=%L", cacheFieldName, instantiation.spec())
        .endControlFlow()
        .endControlFlow()
        .endControlFlow()
        .add("return ( %N as %T )", cacheFieldName, returnTypeName.reloadedForTypeArgs(env))
        .build()
  }

  private fun motif.compiler.TypeName.reloadedForTypeArgs(env: XProcessingEnv): TypeName =
      if (kt is ParameterizedTypeName) {
        kt
      } else {
        // ensures that type arguments get loaded
        KotlinTypeWorkaround.javaToKotlinType(env.requireType(j))
      }

  private fun FactoryProviderMethodBody.Uncached.spec(): CodeBlock =
      CodeBlock.of("return %L", instantiation.spec())

  private fun FactoryProviderInstantiation.spec(): CodeBlock =
      when (this) {
        is FactoryProviderInstantiation.Basic -> spec()
        is FactoryProviderInstantiation.Constructor -> spec()
        is FactoryProviderInstantiation.Binds -> spec()
      }

  private fun FactoryProviderInstantiation.Basic.spec(): CodeBlock {
    val methodName = factoryMethodName.substringBeforeLast('$')
    return if (isStatic) {
      CodeBlock.of("%T.%N%L", objectsClassName.kt, methodName, callProviders.spec())
    } else {
      CodeBlock.of("%N.%N%L", objectsFieldName, methodName, callProviders.spec())
    }
  }

  private fun FactoryProviderInstantiation.Constructor.spec(): CodeBlock =
      CodeBlock.of("%T%L", returnTypeName.kt, callProviders.spec())

  private fun FactoryProviderInstantiation.Binds.spec(): CodeBlock =
      CodeBlock.of("%N()", providerMethodName)

  private fun CallProviders.spec(): String {
    val callString = providerMethodNames.joinToString { "$it()" }
    return "($callString)"
  }

  private fun SpreadProviderMethod.spec(): FunSpec =
      FunSpec.builder(name)
          .apply {
            returns(returnTypeName.kt)
            if (isStatic) {
              addStatement("return %T.%N()", sourceTypeName.kt, spreadMethodName)
            } else {
              addStatement("return %N().%N()", sourceProviderMethodName, spreadMethodName)
            }
          }
          .build()

  private fun DependencyProviderMethod.spec(): FunSpec =
      FunSpec.builder(name)
          .addModifiers(KModifier.INTERNAL)
          .returns(returnTypeName.kt)
          .addStatement("return %N.%N()", dependenciesFieldName, dependencyMethodName)
          .build()

  private fun Dependencies.spec(): TypeSpec {
    val typeSpecBuilder =
        if (methods.any { it.internal }) {
          TypeSpec.classBuilder(className.kt).addModifiers(KModifier.ABSTRACT)
        } else {
          TypeSpec.interfaceBuilder(className.kt)
        }
    return typeSpecBuilder.apply { methods.forEach { addFunction(it.spec()) } }.build()
  }

  private fun DependencyMethod.spec(): FunSpec =
      FunSpec.builder(name)
          .apply {
            qualifier?.let { addAnnotation(it.spec()) }
            addModifiers(if (internal) KModifier.INTERNAL else KModifier.PUBLIC)
            addModifiers(KModifier.ABSTRACT)
            returns(returnTypeName.kt)
            addKdoc(javaDoc.spec())
          }
          .build()

  private fun Qualifier.spec(): AnnotationSpec {
    val className =
        annotation.mirror.type.typeElement?.className?.toKClassName()
            ?: throw IllegalStateException("No ClassName found for: ${annotation.mirror.type}")
    return AnnotationSpec.builder(className)
        .apply {
          annotation.mirror.annotationValues.forEach {
            it.value?.let { value -> addMember("%S", value) }
          }
        }
        .build()
  }

  private fun DependencyMethodJavaDoc.spec(): CodeBlock =
      CodeBlock.builder()
          .apply {
            add("\nRequested from:\n")
            requestedFrom.forEach { add(it.spec()) }
            add("\n")
          }
          .build()

  private fun JavaDocMethodLink.spec(): CodeBlock = CodeBlock.of("* [%L.%N]\n", owner, methodName)

  private fun ObjectsImpl.spec(): TypeSpec =
      TypeSpec.classBuilder(className.kt)
          .apply {
            addModifiers(KModifier.PRIVATE)
            if (isInterface) {
              addSuperinterface(superClassName.kt)
            } else {
              superclass(superClassName.kt)
            }
            abstractMethods.forEach { addFunction(it.spec()) }
          }
          .build()

  private fun ObjectsAbstractMethod.spec(): FunSpec =
      XFunSpec.overriding(overriddenMethod.element, overriddenMethod.owner, env)
          .addStatement("throw %T()", UnsupportedOperationException::class)
          .build()

  private fun suppressAnnotationSpec(vararg names: String): AnnotationSpec =
      AnnotationSpec.builder(Suppress::class.java)
          .addMember(names.joinToString(", ") { "%S" }, *names)
          .build()
}
