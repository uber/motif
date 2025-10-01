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

  private fun ScopeImpl.spec(): TypeSpec =
      TypeSpec.classBuilder(className.j)
          .apply {
            addAnnotation(scopeImplAnnotation.spec())
            addModifiers(Modifier.PUBLIC)
            addSuperinterface(superClassName.j)
            objectsField?.let { addField(it.spec()) }
            addField(dependenciesField.spec())
            cacheFields.forEach { addField(it.spec(useNullFieldInitialization)) }
            addMethod(constructor.spec())
            alternateConstructor?.let { addMethod(it.spec()) }
            accessMethodImpls.forEach { addMethod(it.spec()) }
            childMethodImpls.forEach { addMethod(it.spec()) }
            addMethod(scopeProviderMethod.spec())
            factoryProviderMethods.forEach { addMethods(it.specs(useNullFieldInitialization)) }
            dependencyProviderMethods.forEach { addMethod(it.spec()) }
            dependencies?.let { addType(it.spec()) }
            objectsImpl?.let { addType(it.spec()) }
          }
          .build()

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

  private fun CacheField.spec(useNullFieldInitialization: Boolean): FieldSpec =
      if (useNullFieldInitialization) {
        FieldSpec.builder(Object::class.java, name, Modifier.PRIVATE, Modifier.VOLATILE).build()
      } else {
        FieldSpec.builder(Object::class.java, name, Modifier.PRIVATE, Modifier.VOLATILE)
            .initializer("\$T.NONE", None::class.java)
            .build()
      }

  private fun Constructor.spec(): MethodSpec =
      MethodSpec.constructorBuilder()
          .addModifiers(Modifier.PUBLIC)
          .addParameter(dependenciesClassName.j, dependenciesParameterName)
          .addStatement("this.\$N = \$N", dependenciesFieldName, dependenciesParameterName)
          .build()

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
  private fun ChildDependenciesImpl.spec(): TypeSpec {
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

  private fun FactoryProviderMethod.specs(useNullFieldInitialization: Boolean): List<MethodSpec> {
    val primarySpec =
        MethodSpec.methodBuilder(name)
            .returns(returnTypeName.j)
            .addStatement(body.spec(useNullFieldInitialization))
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
      return CodeBlock.builder()
          // Using a local variable reduces atomic read overhead
          .add("Object $localFieldName = \$N;\n", cacheFieldName)
          .beginControlFlow("if (\$N == null)", localFieldName)
          .beginControlFlow("synchronized (this)")
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
    return CodeBlock.builder()
        .beginControlFlow("if (\$N == \$T.NONE)", cacheFieldName, None::class.java)
        .beginControlFlow("synchronized (this)")
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

  private fun ObjectsImpl.spec(): TypeSpec =
      TypeSpec.classBuilder(className.j)
          .apply {
            addModifiers(Modifier.PRIVATE, Modifier.STATIC)
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
