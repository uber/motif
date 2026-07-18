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

  // [Caching strategy] Routes to wrapper, variant, or standard spec.
  private fun ScopeImpl.spec(): TypeSpec {
    if (isRuntimeSelectableWrapper) {
      return wrapperSpec()
    }

    val classNameToUse =
        if (variantSuffix != null) {
          com.squareup.kotlinpoet.ClassName(
              className.kt.packageName,
              className.kt.simpleName + variantSuffix,
          )
        } else {
          className.kt
        }

    return spec(classNameToUse)
  }

  // Builds the class TypeSpec for a ScopeImpl.
  private fun ScopeImpl.spec(classNameToUse: com.squareup.kotlinpoet.ClassName): TypeSpec =
      TypeSpec.classBuilder(classNameToUse)
          .apply {
            addAnnotation(suppressAnnotationSpec("REDUNDANT_PROJECTION", "UNCHECKED_CAST"))
            addAnnotation(scopeImplAnnotation.spec())
            addModifiers(if (internalScope) KModifier.INTERNAL else KModifier.PUBLIC)
            addSuperinterface(superClassName.kt)
            objectsField?.let { addProperty(it.spec()) }
            addProperty(dependenciesField.spec())
            cacheFields.forEach { addProperty(it.spec(isBaselineStrategy = isBaselineStrategy)) }
            addPerDependencyLockFields(perDependencyLockFields)
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
            factoryProviderMethods.forEach {
              addFunctions(
                  it.specs(
                      isBaselineStrategy = isBaselineStrategy,
                      perDependencyLockFields = perDependencyLockFields,
                  ),
              )
            }
            dependencyProviderMethods.forEach { addFunction(it.spec()) }
            dependencies?.let { addType(it.spec()) }
            // RUNTIME_SELECTABLE generates sibling variant classes that share Objects, requiring
            // wider visibility
            objectsImpl?.let { addType(it.spec(widenObjectsVisibility = variantSuffix != null)) }
          }
          .build()

  // [Caching strategy] Adds MotifLock fields and config cache when per-dependency locking is
  // enabled.
  private fun TypeSpec.Builder.addPerDependencyLockFields(lockFields: PerDependencyLockFields?) {
    lockFields ?: return
    if (lockFields.locks.isNotEmpty()) {
      addProperty(
          PropertySpec.builder("usePerDependencyLocking", Boolean::class)
              .addModifiers(KModifier.PRIVATE)
              .initializer(
                  "%T.usePerDependencyLock",
                  ClassName.bestGuess("motif.MotifRuntimeConfig"),
              )
              .build(),
      )
    }
    lockFields.locks.values.forEach { lockFieldName ->
      addProperty(
          PropertySpec.builder(
                  lockFieldName,
                  ClassName.bestGuess("motif.MotifLock").copy(nullable = true),
              )
              .addModifiers(KModifier.PRIVATE)
              .mutable(false)
              .initializer(
                  "if (usePerDependencyLocking) %T() else null",
                  ClassName.bestGuess("motif.MotifLock"),
              )
              .build(),
      )
    }
  }

  /**
   * Generates a runtime wrapper class for RUNTIME_SELECTABLE strategy. The wrapper delegates to
   * variant implementations based on MotifRuntimeConfig.cachingStrategy.
   */
  private fun ScopeImpl.wrapperSpec(): TypeSpec {
    // Creates a condition block to choose between SmartCache and BaselineSelectableLock
    val wrapperConstructor =
        FunSpec.constructorBuilder()
            .addParameter(dependenciesField.name, dependenciesField.dependenciesClassName.kt)
            .build()

    return TypeSpec.classBuilder(className.kt)
        .apply {
          addAnnotation(suppressAnnotationSpec("REDUNDANT_PROJECTION", "UNCHECKED_CAST"))
          addAnnotation(scopeImplAnnotation.spec())
          addModifiers(if (internalScope) KModifier.INTERNAL else KModifier.PUBLIC)
          addSuperinterface(superClassName.kt)

          addProperty(dependenciesField.spec())

          addProperty(
              PropertySpec.builder("delegate", superClassName.kt, KModifier.PRIVATE)
                  .initializer(
                      CodeBlock.builder()
                          .add(
                              "when (%T.cachingStrategy) {\n",
                              ClassName.bestGuess("motif.MotifRuntimeConfig"),
                          )
                          .indent()
                          .add(
                              "%T.SMART_CACHE -> %T_SmartCache(dependencies)\n",
                              ClassName.bestGuess("motif.CachingStrategy"),
                              className.kt,
                          )
                          .add("else -> %T_BaselineSelectableLock(dependencies)\n", className.kt)
                          .unindent()
                          .add("}")
                          .build(),
                  )
                  .build(),
          )

          primaryConstructor(wrapperConstructor)
          alternateConstructor?.let { addFunction(it.spec()) }

          accessMethodImpls
              .filter { !it.overriddenMethod.isSynthetic }
              .forEach {
                addFunction(
                    XFunSpec.overriding(
                            it.overriddenMethod.element,
                            it.overriddenMethod.owner,
                            it.env,
                        )
                        .addStatement("return delegate.%N()", it.overriddenMethod.name)
                        .build(),
                )
              }

          // Delegate synthetic properties
          accessMethodImpls
              .filter { it.overriddenMethod.isSynthetic }
              .forEach {
                val propName =
                    with(it.overriddenMethod.name) {
                      when {
                        startsWith("get") -> this.substring(3).decapitalize()
                        startsWith("is") -> this.substring(2).decapitalize()
                        else -> this
                      }
                    }
                addProperty(
                    PropertySpec.builder(
                            propName,
                            ClassName.bestGuess(it.overriddenMethod.returnType.qualifiedName),
                        )
                        .addModifiers(KModifier.OVERRIDE)
                        .initializer("delegate.%N", propName)
                        .build(),
                )
              }

          // Delegate child methods
          childMethodImpls.forEach { childMethod ->
            addFunction(
                FunSpec.builder(childMethod.childMethodName)
                    .addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE)
                    .returns(childMethod.childClassName.kt)
                    .apply {
                      childMethod.parameters.forEach { param ->
                        addParameter(param.name, param.typeName.kt)
                      }
                    }
                    .addStatement(
                        "return delegate.%N(%L)",
                        childMethod.childMethodName,
                        childMethod.parameters.joinToString { it.name },
                    )
                    .build(),
            )
          }

          // Add Objects nested class if present (for variants to reference)
          objectsImpl?.let { addType(it.spec(widenObjectsVisibility = true)) }

          // Add Dependencies interface so variant implementations can reference it
          dependencies?.let { addType(it.spec()) }
        }
        .build()
  }

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

  private fun CacheField.spec(isBaselineStrategy: Boolean): PropertySpec =
      if (isBaselineStrategy) {
        // BASELINE: Use None.NONE sentinel
        PropertySpec.builder(name, Any::class, KModifier.PRIVATE)
            .mutable(true)
            .addAnnotation(Volatile::class)
            .initializer("%T.NONE", None::class)
            .build()
      } else {
        // SMART_CACHE: Use null initialization
        PropertySpec.builder(name, Any::class.asTypeName().copy(true), KModifier.PRIVATE)
            .mutable(true)
            .addAnnotation(Volatile::class)
            .initializer("null")
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

  // Inline anonymous class that wires a child scope's Dependencies to the parent graph.
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

  private fun FactoryProviderMethod.specs(
      isBaselineStrategy: Boolean,
      perDependencyLockFields: PerDependencyLockFields?,
  ): List<FunSpec> {
    val primarySpec =
        FunSpec.builder(name)
            .addModifiers(KModifier.INTERNAL)
            .returns(returnTypeName.reloadedForTypeArgs(env))
            .addCode(body.spec(isBaselineStrategy, perDependencyLockFields))
            .build()
    val spreadSpecs = spreadProviderMethods.map { it.spec() }
    return listOf(primarySpec) + spreadSpecs
  }

  private fun FactoryProviderMethodBody.spec(
      isBaselineStrategy: Boolean,
      perDependencyLockFields: PerDependencyLockFields?,
  ): CodeBlock =
      when (this) {
        is FactoryProviderMethodBody.Cached -> spec(isBaselineStrategy, perDependencyLockFields)
        is FactoryProviderMethodBody.Uncached -> spec()
      }

  private fun FactoryProviderMethodBody.Cached.spec(
      isBaselineStrategy: Boolean,
      perDependencyLockFields: PerDependencyLockFields?,
  ): CodeBlock {
    // SMART_CACHE strategy: Use null initialization instead of None.NONE sentinel
    // This path is taken when cachingStrategy == SMART_CACHE
    if (!isBaselineStrategy) {
      val localFieldName = "_$cacheFieldName"
      // Get the lock field name for this cache field (if per-dependency locks are enabled)
      val lockFieldName = perDependencyLockFields?.locks?.get(cacheFieldName)

      val codeBlockBuilder =
          CodeBlock.builder()
              // Using a local variable reduces atomic read overhead
              .addStatement("var $localFieldName = %N;\n", cacheFieldName)
              .beginControlFlow("if (%N == null)", localFieldName)

      // Add synchronized block using nullable lock pattern: lock_foo ?: this
      if (lockFieldName != null) {
        codeBlockBuilder.beginControlFlow("synchronized(%N ?: this)", lockFieldName)
      } else {
        codeBlockBuilder.beginControlFlow("synchronized (this)")
      }

      codeBlockBuilder
          .addStatement("%N = %N", localFieldName, cacheFieldName)
          .beginControlFlow("if (%N == null)", localFieldName)
          .addStatement("%N = %L", localFieldName, instantiation.spec())
          .beginControlFlow("if (%N == null)", localFieldName)
          .addStatement(
              "throw %T(%S)",
              NullPointerException::class,
              "Factory method cannot return null",
          )
          .endControlFlow()
          .addStatement("%N = %N", cacheFieldName, localFieldName)
          .endControlFlow()
          .endControlFlow()
          .endControlFlow()
      return codeBlockBuilder
          .add("return ( %N as %T )", localFieldName, returnTypeName.reloadedForTypeArgs(env))
          .build()
    }

    // BASELINE strategy: Use None.NONE sentinel with synchronized(this) - matches alpha09 exactly
    // Get the lock field name for this cache field (if per-dependency locks are enabled)
    val lockFieldName = perDependencyLockFields?.locks?.get(cacheFieldName)

    val codeBuilder =
        CodeBlock.builder().beginControlFlow("if (%N == %T.NONE)", cacheFieldName, None::class)

    // Add synchronized block using nullable lock pattern: lock_foo ?: this
    if (lockFieldName != null) {
      codeBuilder.beginControlFlow("synchronized(%N ?: this)", lockFieldName)
    } else {
      codeBuilder.beginControlFlow("synchronized (this)")
    }

    return codeBuilder
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

  private fun ObjectsImpl.spec(widenObjectsVisibility: Boolean): TypeSpec =
      TypeSpec.classBuilder(className.kt)
          .apply {
            if (widenObjectsVisibility) {
              // internal so RUNTIME_SELECTABLE variants, which are separate top-level
              // classes, can access it.
              addModifiers(KModifier.INTERNAL)
            } else {
              addModifiers(KModifier.PRIVATE)
            }
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
