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
import motif.internal.None
import javax.lang.model.element.Modifier

object JavaCodeGenerator {

    fun generate(scopeImpl: ScopeImpl): JavaFile {
        val typeSpec: TypeSpec = scopeImpl.spec()
        return JavaFile.builder(scopeImpl.className.j.packageName(), typeSpec).build()
    }

    private fun ScopeImpl.spec(): TypeSpec {
        return TypeSpec.classBuilder(className.j).apply {
            addAnnotation(scopeImplAnnotation.spec())
            addModifiers(Modifier.PUBLIC)
            addSuperinterface(superClassName.j)
            objectsField?.let { addField(it.spec()) }
            addField(dependenciesField.spec())
            cacheFields.forEach { addField(it.spec()) }
            addMethod(constructor.spec())
            alternateConstructor?.let { addMethod(it.spec()) }
            accessMethodImpls.forEach { addMethod(it.spec()) }
            childMethodImpls.forEach { addMethod(it.spec()) }
            addMethod(scopeProviderMethod.spec())
            factoryProviderMethods.forEach { addMethods(it.specs()) }
            dependencyProviderMethods.forEach { addMethod(it.spec()) }
            dependencies?.let { addType(it.spec()) }
            objectsImpl?.let { addType(it.spec()) }
        }.build()
    }

    private fun ScopeImplAnnotation.spec(): AnnotationSpec {
        return AnnotationSpec.builder(motif.ScopeImpl::class.java).apply {
            if (children.isEmpty()) {
                addMember("children", "{}")
            } else {
                children.forEach { child -> addMember("children", "\$T.class", child.j) }
            }
            addMember("scope", "\$T.class", scopeClassName.j)
            addMember("dependencies", "\$T.class", dependenciesClassName.j)
        }.build()
    }

    private fun ObjectsField.spec(): FieldSpec {
        return FieldSpec.builder(objectsClassName.j, name, Modifier.PRIVATE, Modifier.FINAL)
                .initializer("new \$T()", objectsImplClassName.j)
                .build()
    }

    private fun DependenciesField.spec(): FieldSpec {
        return FieldSpec.builder(dependenciesClassName.j, name, Modifier.PRIVATE, Modifier.FINAL).build()
    }

    private fun CacheField.spec(): FieldSpec {
        return FieldSpec.builder(Object::class.java, name, Modifier.PRIVATE, Modifier.VOLATILE)
                .initializer("\$T.NONE", None::class.java)
                .build()
    }

    private fun Constructor.spec(): MethodSpec {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(dependenciesClassName.j, dependenciesParameterName)
                .addStatement("this.\$N = \$N", dependenciesFieldName, dependenciesParameterName)
                .build()
    }

    private fun AlternateConstructor.spec(): MethodSpec {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("this(new \$T() {})", dependenciesClassName.j)
                .build()
    }

    private fun AccessMethodImpl.spec(): MethodSpec {
        return MethodSpec.overriding(overriddenMethod.element, overriddenMethod.owner, env.typeUtils)
                .addStatement("return \$N()", providerMethodName)
                .build()
    }

    private fun ChildMethodImpl.spec(): MethodSpec {
        return MethodSpec.methodBuilder(childMethodName).apply {
            addAnnotation(Override::class.java)
            addModifiers(Modifier.PUBLIC)
            returns(childClassName.j)
            parameters.forEach { addParameter(it.spec()) }
            addStatement("return new \$T(\$L)", childImplClassName.j, childDependenciesImpl.spec())
        }.build()
    }

    private fun ChildDependenciesImpl.spec(): TypeSpec {
        return TypeSpec.anonymousClassBuilder("").apply {
            addSuperinterface(childDependenciesClassName.j)
            methods.forEach { addMethod(it.spec()) }
        }.build()
    }

    private fun ChildDependencyMethodImpl.spec(): MethodSpec {
        return MethodSpec.methodBuilder(name)
                .addAnnotation(Override::class.java)
                .addModifiers(Modifier.PUBLIC)
                .returns(returnTypeName.j)
                .addStatement(returnExpression.spec())
                .build()
    }

    private fun ChildDependencyMethodImpl.ReturnExpression.spec(): CodeBlock {
        return when (this) {
            is ChildDependencyMethodImpl.ReturnExpression.Parameter -> spec()
            is ChildDependencyMethodImpl.ReturnExpression.Provider -> spec()
        }
    }

    private fun ChildDependencyMethodImpl.ReturnExpression.Parameter.spec(): CodeBlock {
        return CodeBlock.of("return \$N", parameterName)
    }

    private fun ChildDependencyMethodImpl.ReturnExpression.Provider.spec(): CodeBlock {
        return CodeBlock.of("return \$T.this.\$N()", scopeImplName.j, providerName)
    }

    private fun ChildMethodImplParameter.spec(): ParameterSpec {
        return ParameterSpec.builder(typeName.j, name, Modifier.FINAL).build()
    }

    private fun ScopeProviderMethod.spec(): MethodSpec {
        return MethodSpec.methodBuilder(name)
                .returns(scopeClassName.j)
                .addStatement("return this")
                .build()
    }

    private fun FactoryProviderMethod.specs(): List<MethodSpec> {
        val primarySpec = MethodSpec.methodBuilder(name)
                .returns(returnTypeName.j)
                .addStatement(body.spec())
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

    private fun FactoryProviderMethodBody.Uncached.spec(): CodeBlock {
        return CodeBlock.of("return \$L", instantiation.spec())
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
            CodeBlock.of("\$T.\$N\$L", objectsClassName.j, factoryMethodName, callProviders.spec())
        } else {
            CodeBlock.of("\$N.\$N\$L", objectsFieldName, factoryMethodName, callProviders.spec())
        }
    }

    private fun FactoryProviderInstantiation.Constructor.spec(): CodeBlock {
        return CodeBlock.of("new \$T\$L", returnTypeName.j, callProviders.spec())
    }

    private fun FactoryProviderInstantiation.Binds.spec(): CodeBlock {
        return CodeBlock.of("\$N()", providerMethodName)
    }

    private fun CallProviders.spec(): String {
        val callString = providerMethodNames.joinToString { "$it()" }
        return "($callString)"
    }

    private fun SpreadProviderMethod.spec(): MethodSpec {
        return MethodSpec.methodBuilder(name)
                .returns(returnTypeName.j)
                .addStatement("return \$N().\$N()", sourceProviderMethodName, spreadMethodName)
                .build()
    }

    private fun DependencyProviderMethod.spec(): MethodSpec {
        return MethodSpec.methodBuilder(name)
                .returns(returnTypeName.j)
                .addStatement("return \$N.\$N()", dependenciesFieldName, dependencyMethodName)
                .build()
    }

    private fun Dependencies.spec(): TypeSpec {
        return TypeSpec.interfaceBuilder(className.j).apply {
            addModifiers(Modifier.PUBLIC)
            methods.forEach { addMethod(it.spec()) }
        }.build()
    }

    private fun DependencyMethod.spec(): MethodSpec {
        return MethodSpec.methodBuilder(name).apply {
            qualifier?.let { addAnnotation(it.spec()) }
            addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            returns(returnTypeName.j)
            addJavadoc(javaDoc.spec())
        }.build()
    }

    private fun Qualifier.spec(): AnnotationSpec {
        return AnnotationSpec.get(annotation.mirror)
    }

    private fun DependencyMethodJavaDoc.spec(): CodeBlock {
        return CodeBlock.builder().apply {
            add("<ul>\nRequested from:\n")
            requestedFrom.forEach { add(it.spec()) }
            add("</ul>\n")
        }.build()
    }

    private fun JavaDocMethodLink.spec(): CodeBlock {
        val parameterTypeString = parameterTypes.joinToString()
        return CodeBlock.of("<li>{@link \$L#\$N(\$L)}</li>\n", owner, methodName, parameterTypeString)
    }

    private fun ObjectsImpl.spec(): TypeSpec {
        return TypeSpec.classBuilder(className.j).apply {
            addModifiers(Modifier.PRIVATE, Modifier.STATIC)
            if (isInterface) {
                addSuperinterface(superClassName.j)
            } else {
                superclass(superClassName.j)
            }
            abstractMethods.forEach { addMethod(it.spec()) }
        }.build()
    }

    private fun ObjectsAbstractMethod.spec(): MethodSpec {
        return MethodSpec.overriding(overriddenMethod.element, overriddenMethod.owner, env.typeUtils)
                .addStatement("throw new \$T()", UnsupportedOperationException::class.java)
                .build()
    }
}