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
import androidx.room.compiler.processing.compat.XConverters.toJavac
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeSpec
import com.squareup.kotlinpoet.javapoet.KotlinPoetJavaPoetPreview
import com.uber.xprocessing.ext.isKotlinSource
import com.uber.xprocessing.ext.withRawTypeFix
import javax.lang.model.element.Modifier
import javax.lang.model.type.DeclaredType
import motif.internal.None

object JavaCodeGenerator {

  fun generate(scopeImpl: ScopeImpl): JavaFile {
    val typeSpec: TypeSpec = scopeImpl.spec()
    return JavaFile.builder(scopeImpl.className.j.packageName(), typeSpec).build()
  }

  // [Caching strategy] Routes to wrapper, variant, or standard spec.
  private fun ScopeImpl.spec(): TypeSpec {
    if (isRuntimeSelectableWrapper) {
      return wrapperSpec()
    }

    val classNameToUse = if (variantSuffix != null) {
      com.squareup.javapoet.ClassName.get(
          className.j.packageName(),
          className.j.simpleName() + variantSuffix
      )
    } else {
      className.j
    }

    return spec(classNameToUse)
  }

  // Builds the class TypeSpec for a ScopeImpl.
  private fun ScopeImpl.spec(classNameToUse: com.squareup.javapoet.ClassName): TypeSpec =
      TypeSpec.classBuilder(classNameToUse)
          .apply {
            addAnnotation(scopeImplAnnotation.spec())
            addModifiers(Modifier.PUBLIC)
            addSuperinterface(superClassName.j)
            objectsField?.let { addField(it.spec()) }
            addField(dependenciesField.spec())
            cacheFields.forEach { addField(it.spec(isBaselineStrategy = isBaselineStrategy)) }
            addPerDependencyLockFields(perDependencyLockFields)
            addMethod(constructor.spec(perDependencyLockFields))
            alternateConstructor?.let { addMethod(it.spec()) }
            accessMethodImpls.forEach { addMethod(it.spec()) }
            childMethodImpls.forEach { addMethod(it.spec()) }
            addMethod(scopeProviderMethod.spec())
            factoryProviderMethods.forEach { addMethods(it.specs(isBaselineStrategy = isBaselineStrategy, perDependencyLockFields = perDependencyLockFields)) }
            dependencyProviderMethods.forEach { addMethod(it.spec()) }
            dependencies?.let { addType(it.spec()) }
            // RUNTIME_SELECTABLE generates sibling variant classes that share Objects, requiring wider visibility
            objectsImpl?.let { addType(it.spec(widenObjectsVisibility = variantSuffix != null)) }
          }
          .build()

  // [Caching strategy] Adds MotifLock fields when per-dependency locking is enabled.
  private fun TypeSpec.Builder.addPerDependencyLockFields(lockFields: ScopeImpl.PerDependencyLockFields?) {
    lockFields?.locks?.values?.forEach { lockFieldName ->
      addField(
          FieldSpec.builder(com.squareup.javapoet.ClassName.get("motif", "MotifLock"), lockFieldName, Modifier.PRIVATE, Modifier.FINAL)
              .build()
      )
    }
  }

  /**
   * Generates a runtime wrapper class for RUNTIME_SELECTABLE strategy.
   * The wrapper delegates to variant implementations based on MotifRuntimeConfig.cachingStrategy.
   */
  private fun ScopeImpl.wrapperSpec(): TypeSpec {
    val delegateField = FieldSpec.builder(superClassName.j, "delegate", Modifier.PRIVATE, Modifier.FINAL)
        .build()

    //Create constructor that initializes delegate based on runtime config
    val wrapperConstructor = MethodSpec.constructorBuilder()
        .addModifiers(Modifier.PUBLIC)
        .addParameter(dependenciesField.dependenciesClassName.j, "dependencies")
        .addStatement("this.dependencies = dependencies")
        .beginControlFlow("if (\$T.cachingStrategy == \$T.SMART_CACHE)",
            com.squareup.javapoet.ClassName.get("motif", "MotifRuntimeConfig"),
            com.squareup.javapoet.ClassName.get("motif", "CachingStrategy"))
        .addStatement("this.delegate = new \$T(dependencies)",
            com.squareup.javapoet.ClassName.get(
                className.j.packageName(),
                className.j.simpleName() + "_SmartCache"
            ))
        .nextControlFlow("else")
        .addStatement("this.delegate = new \$T(dependencies)",
            com.squareup.javapoet.ClassName.get(
                className.j.packageName(),
                className.j.simpleName() + "_BaselineSelectableLock"
            ))
        .endControlFlow()
        .build()

    return TypeSpec.classBuilder(className.j)
        .apply {
          addAnnotation(scopeImplAnnotation.spec())
          addModifiers(Modifier.PUBLIC)
          addSuperinterface(superClassName.j)

          // Add delegate field
          addField(delegateField)

          // Add dependencies field
          addField(dependenciesField.spec())

          // Add custom wrapper constructor
          addMethod(wrapperConstructor)

          // Add alternate constructor if present
          alternateConstructor?.let { addMethod(it.spec()) }

          // Delegate all access methods
          accessMethodImpls.forEach { accessMethod ->
            addMethod(
                MethodSpec.overriding(
                        accessMethod.overriddenMethod.element.toJavac(),
                        accessMethod.overriddenMethod.owner.toJavac() as DeclaredType,
                        accessMethod.env.toJavac().typeUtils,
                    )
                    .addStatement("return delegate.\$N()", accessMethod.overriddenMethod.name)
                    .build()
            )
          }

          // Delegate all child methods
          childMethodImpls.forEach { childMethod ->
            addMethod(
                MethodSpec.methodBuilder(childMethod.childMethodName)
                    .addAnnotation(Override::class.java)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(childMethod.childClassName.j)
                    .apply {
                      childMethod.parameters.forEach { param ->
                        addParameter(param.spec())
                      }
                    }
                    .addStatement(
                        "return delegate.\$N(\$L)",
                        childMethod.childMethodName,
                        childMethod.parameters.joinToString(", ") { it.name }
                    )
                    .build()
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
      AnnotationSpec.builder(motif.ScopeImpl::class.java)
          .apply {
            if (children.isEmpty()) {
              addMember("children", "{}")
            } else {
              children.forEach { child -> addMember("children", "\$T.class", child.j) }
            }
            addMember("scope", "\$T.class", scopeClassName.j)
            addMember("dependencies", "\$T.class", dependenciesClassName.j)
          }
          .build()

  private fun ObjectsField.spec(): FieldSpec =
      FieldSpec.builder(objectsClassName.j, name, Modifier.PRIVATE, Modifier.FINAL)
          .initializer("new \$T()", objectsImplClassName.j)
          .build()

  private fun DependenciesField.spec(): FieldSpec =
      FieldSpec.builder(dependenciesClassName.j, name, Modifier.PRIVATE, Modifier.FINAL).build()

  private fun CacheField.spec(isBaselineStrategy: Boolean): FieldSpec =
      if (isBaselineStrategy) {
        // BASELINE: Use None.NONE sentinel
        FieldSpec.builder(Object::class.java, name, Modifier.PRIVATE, Modifier.VOLATILE)
            .initializer("\$T.NONE", None::class.java)
            .build()
      } else {
        // SMART_CACHE: Use null initialization
        FieldSpec.builder(Object::class.java, name, Modifier.PRIVATE, Modifier.VOLATILE).build()
      }

  private fun Constructor.spec(perDependencyLockFields: PerDependencyLockFields?): MethodSpec {
    val builder = MethodSpec.constructorBuilder()
        .addModifiers(Modifier.PUBLIC)
        .addParameter(dependenciesClassName.j, dependenciesParameterName)
        .addStatement("this.\$N = \$N", dependenciesFieldName, dependenciesParameterName)

    // Use local variable to read runtime config once for consistent lock initialization
    if (perDependencyLockFields != null && perDependencyLockFields.locks.isNotEmpty()) {
        builder.addStatement("boolean usePerDependencyLocking = \$T.usePerDependencyLock",
            com.squareup.javapoet.ClassName.get("motif", "MotifRuntimeConfig"))
    }

    // Initialize lock fields conditionally based on usePerDependencyLocking
    perDependencyLockFields?.locks?.values?.forEach { lockFieldName ->
        builder.addStatement("this.\$N = usePerDependencyLocking ? new \$T() : null", lockFieldName, com.squareup.javapoet.ClassName.get("motif", "MotifLock"))
    }

    return builder.build()
  }

  private fun AlternateConstructor.spec(): MethodSpec =
      MethodSpec.constructorBuilder()
          .addModifiers(Modifier.PUBLIC)
          .addStatement("this(new \$T() {})", dependenciesClassName.j)
          .build()

  private fun AccessMethodImpl.spec(): MethodSpec =
      MethodSpec.overriding(
              overriddenMethod.element.toJavac(),
              overriddenMethod.owner.toJavac() as DeclaredType,
              env.toJavac().typeUtils,
          )
          .addStatement("return \$N()", providerMethodName)
          .build()

  private fun ChildMethodImpl.spec(): MethodSpec =
      MethodSpec.methodBuilder(childMethodName)
          .apply {
            addAnnotation(Override::class.java)
            addModifiers(Modifier.PUBLIC)
            returns(childClassName.j)
            this@spec.parameters.forEach { addParameter(it.spec()) }
            addStatement("return new \$T(\$L)", childImplClassName.j, childDependenciesImpl.spec())
          }
          .build()

  @OptIn(KotlinPoetJavaPoetPreview::class)
  // Inline anonymous class that wires a child scope's Dependencies to the parent graph.
  private fun ChildDependenciesImpl.spec(): CodeBlock = CodeBlock.of("\$L", spec_anonymousClass())

  @OptIn(KotlinPoetJavaPoetPreview::class)
  private fun ChildDependenciesImpl.spec_anonymousClass(): TypeSpec {
    val isKotlinDepInterface = env.findTypeElement(childDependenciesClassName.j).isKotlinSource(env)
    return TypeSpec.anonymousClassBuilder("")
        .apply {
          addSuperinterface(childDependenciesClassName.j)
          methods.forEach { addMethod(it.spec(env, isKotlinDepInterface)) }
        }
        .build()
  }

  private fun ChildDependencyMethodImpl.spec(
      env: XProcessingEnv,
      isKotlinDependenciesInterface: Boolean,
  ): MethodSpec =
      MethodSpec.methodBuilder(name)
          .addAnnotation(Override::class.java)
          .addModifiers(Modifier.PUBLIC)
          .returns(
              if (isKotlinDependenciesInterface) {
                returnTypeName.j.withRawTypeFix(env)
              } else {
                returnTypeName.j
              },
          )
          .addStatement(returnExpression.spec())
          .build()

  private fun ChildDependencyMethodImpl.ReturnExpression.spec(): CodeBlock =
      when (this) {
        is ChildDependencyMethodImpl.ReturnExpression.Parameter -> spec()
        is ChildDependencyMethodImpl.ReturnExpression.Provider -> spec()
      }

  private fun ChildDependencyMethodImpl.ReturnExpression.Parameter.spec(): CodeBlock =
      CodeBlock.of("return \$N", parameterName)

  private fun ChildDependencyMethodImpl.ReturnExpression.Provider.spec(): CodeBlock =
      CodeBlock.of("return \$T.this.\$N()", scopeImplName.j, providerName)

  private fun ChildMethodImplParameter.spec(): ParameterSpec =
      ParameterSpec.builder(typeName.j, name, Modifier.FINAL).build()

  private fun ScopeProviderMethod.spec(): MethodSpec =
      MethodSpec.methodBuilder(name).returns(scopeClassName.j).addStatement("return this").build()

  private fun FactoryProviderMethod.specs(
      isBaselineStrategy: Boolean,
      perDependencyLockFields: PerDependencyLockFields?
  ): List<MethodSpec> {
    val primarySpec =
        MethodSpec.methodBuilder(name)
            .returns(returnTypeName.j)
            .addStatement(body.spec(isBaselineStrategy, perDependencyLockFields))
            .build()
    val spreadSpecs = spreadProviderMethods.map { it.spec() }
    return listOf(primarySpec) + spreadSpecs
  }

  private fun FactoryProviderMethodBody.spec(
      isBaselineStrategy: Boolean,
      perDependencyLockFields: PerDependencyLockFields?
  ): CodeBlock =
      when (this) {
        is FactoryProviderMethodBody.Cached -> spec(isBaselineStrategy, perDependencyLockFields)
        is FactoryProviderMethodBody.Uncached -> spec()
      }

  private fun FactoryProviderMethodBody.Cached.spec(
      isBaselineStrategy: Boolean,
      perDependencyLockFields: PerDependencyLockFields?
  ): CodeBlock {
    // SMART_CACHE strategy: Use null initialization
    if (!isBaselineStrategy) {
      val localFieldName = "_$cacheFieldName"
      // Get the lock field name for this cache field (if per-dependency locks are enabled)
      val lockFieldName = perDependencyLockFields?.locks?.get(cacheFieldName)

      val builder = CodeBlock.builder()
          // Using a local variable reduces atomic read overhead
          .add("Object $localFieldName = \$N;\n", cacheFieldName)
          .beginControlFlow("if (\$N == null)", localFieldName)

      // Add synchronized block using nullable lock pattern: lock_foo != null ? lock_foo : this
      if (lockFieldName != null) {
          builder.beginControlFlow("synchronized(\$N != null ? \$N : this)", lockFieldName, lockFieldName)
      } else {
          builder.beginControlFlow("synchronized (this)")
      }

      return builder
          .add("\$N = \$N;\n", localFieldName, cacheFieldName)
          .beginControlFlow("if (\$N == null)", localFieldName)
          .add("\$N = \$L;\n", localFieldName, instantiation.spec())
          .beginControlFlow("if (\$N == null)", localFieldName)
          .add(
              "throw new \$T(\$S);\n",
              NullPointerException::class.java,
              "Factory method cannot return null",
          )
          .endControlFlow()
          .add("\$N = \$N;\n", cacheFieldName, localFieldName)
          .endControlFlow()
          .endControlFlow()
          .endControlFlow()
          .add("return (\$T) \$N", returnTypeName.j, localFieldName)
          .build()
    }
    // BASELINE strategy: Use None.NONE sentinel
    // Get the lock field name for this cache field (if per-dependency locks are enabled)
    val lockFieldName = perDependencyLockFields?.locks?.get(cacheFieldName)

    val builder = CodeBlock.builder()
        .beginControlFlow("if (\$N == \$T.NONE)", cacheFieldName, None::class.java)

    // Add synchronized block using nullable lock pattern: lock_foo != null ? lock_foo : this
    if (lockFieldName != null) {
        builder.beginControlFlow("synchronized(\$N != null ? \$N : this)", lockFieldName, lockFieldName)
    } else {
        builder.beginControlFlow("synchronized (this)")
    }

    return builder
        .beginControlFlow("if (\$N == \$T.NONE)", cacheFieldName, None::class.java)
        .add("\$N = \$L;", cacheFieldName, instantiation.spec())
        .endControlFlow()
        .endControlFlow()
        .endControlFlow()
        .add("return (\$T) \$N", returnTypeName.j, cacheFieldName)
        .build()
  }

  private fun FactoryProviderMethodBody.Uncached.spec(): CodeBlock =
      CodeBlock.of("return \$L", instantiation.spec())

  private fun FactoryProviderInstantiation.spec(): CodeBlock =
      when (this) {
        is FactoryProviderInstantiation.Basic -> spec()
        is FactoryProviderInstantiation.Constructor -> spec()
        is FactoryProviderInstantiation.Binds -> spec()
      }

  private fun FactoryProviderInstantiation.Basic.spec(): CodeBlock =
      if (isStatic) {
        CodeBlock.of("\$T.\$N\$L", objectsClassName.j, factoryMethodName, callProviders.spec())
      } else {
        CodeBlock.of("\$N.\$N\$L", objectsFieldName, factoryMethodName, callProviders.spec())
      }

  private fun FactoryProviderInstantiation.Constructor.spec(): CodeBlock =
      CodeBlock.of("new \$T\$L", returnTypeName.j, callProviders.spec())

  private fun FactoryProviderInstantiation.Binds.spec(): CodeBlock =
      CodeBlock.of("\$N()", providerMethodName)

  private fun CallProviders.spec(): String {
    val callString = providerMethodNames.joinToString { "$it()" }
    return "($callString)"
  }

  private fun SpreadProviderMethod.spec(): MethodSpec =
      MethodSpec.methodBuilder(name)
          .returns(returnTypeName.j)
          .addStatement("return \$N().\$N()", sourceProviderMethodName, spreadMethodName)
          .build()

  private fun DependencyProviderMethod.spec(): MethodSpec =
      MethodSpec.methodBuilder(name)
          .returns(returnTypeName.j)
          .addStatement("return \$N.\$N()", dependenciesFieldName, dependencyMethodName)
          .build()

  private fun Dependencies.spec(): TypeSpec =
      TypeSpec.interfaceBuilder(className.j)
          .apply {
            addModifiers(Modifier.PUBLIC)
            methods.forEach { addMethod(it.spec()) }
          }
          .build()

  private fun DependencyMethod.spec(): MethodSpec =
      MethodSpec.methodBuilder(name)
          .apply {
            qualifier?.let { addAnnotation(it.spec()) }
            addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            returns(returnTypeName.j)
            addJavadoc(javaDoc.spec())
          }
          .build()

  private fun Qualifier.spec(): AnnotationSpec {
    val className =
        annotation.mirror.type.typeElement?.className
            ?: throw IllegalStateException("No ClassName found for: ${annotation.mirror.type}")
    return AnnotationSpec.builder(className)
        .apply {
          annotation.mirror.annotationValues.forEach {
            it.value?.let { value -> addMember(it.name, "\$S", value) }
          }
        }
        .build()
  }

  private fun DependencyMethodJavaDoc.spec(): CodeBlock =
      CodeBlock.builder()
          .apply {
            add("<ul>\nRequested from:\n")
            requestedFrom.forEach { add(it.spec()) }
            add("</ul>\n")
          }
          .build()

  private fun JavaDocMethodLink.spec(): CodeBlock {
    val parameterTypeString = parameterTypes.joinToString()
    return CodeBlock.of("<li>{@link \$L#\$N(\$L)}</li>\n", owner, methodName, parameterTypeString)
  }

  private fun ObjectsImpl.spec(widenObjectsVisibility: Boolean): TypeSpec =
      TypeSpec.classBuilder(className.j)
          .apply {
            if (widenObjectsVisibility) {
              // Package-private (no access modifier) so RUNTIME_SELECTABLE variants,
              // which are separate top-level classes, can access it.
              addModifiers(Modifier.STATIC)
            } else {
              addModifiers(Modifier.PRIVATE, Modifier.STATIC)
            }
            if (isInterface) {
              addSuperinterface(superClassName.j)
            } else {
              superclass(superClassName.j)
            }
            abstractMethods.forEach { addMethod(it.spec()) }
          }
          .build()

  private fun ObjectsAbstractMethod.spec(): MethodSpec =
      MethodSpec.overriding(
              overriddenMethod.element.toJavac(),
              overriddenMethod.owner.toJavac() as DeclaredType,
              env.toJavac().typeUtils,
          )
          .addStatement("throw new \$T()", UnsupportedOperationException::class.java)
          .build()
}
