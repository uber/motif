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

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeSpec
import motif.models.ScopeFactory
import javax.lang.model.element.Modifier

class ScopeFactoryHelper(
        override val packageName: String,
        override val spec: TypeSpec) : GeneratedClass

class ScopeFactoryHelperFactory(
        private val scopeFactory: ScopeFactory,
        private val scopeImplTypeName: ClassName,
        private val scopeImplDependencies: Dependencies) {

    private val scopeFactoryTypeName = scopeFactory.clazz.typeName
    private val packageName = scopeFactoryTypeName.packageName()

    private val typeName = ClassName.get(packageName,  "${scopeFactoryTypeName.simpleNames().joinToString("")}Helper")

    fun create(): ScopeFactoryHelper {
        val spec = TypeSpec.classBuilder(typeName)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(createMethod())
        return ScopeFactoryHelper(packageName, spec.build())
    }

    private fun createMethod(): MethodSpec {
        val dependenciesParam = ParameterSpec.builder(scopeFactory.dependencies.clazz.typeName, "dependencies", Modifier.FINAL)
                .build()
        val builder = MethodSpec.methodBuilder("create")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(scopeFactory.scopeClass.typeName)
                .addParameter(dependenciesParam)
        val dependenciesImplSpec = TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(scopeImplDependencies.typeName)
        scopeImplDependencies.getMethods().forEach { generatedMethod ->
            val userDefinedMethod = scopeFactory.dependencies.getMethod(generatedMethod.type)
                    ?: throw IllegalStateException("Could not find user defined method for type: ${generatedMethod.type}")
            dependenciesImplSpec.addMethod(MethodSpec.methodBuilder(generatedMethod.methodSpec.name)
                    .addAnnotation(Override::class.java)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(generatedMethod.methodSpec.returnType)
                    .addStatement("return \$N.\$N()", dependenciesParam.name, userDefinedMethod.method.name)
                    .build())
        }
        builder.addStatement("return new \$T(\$L)", scopeImplTypeName, dependenciesImplSpec.build())
        return builder.build()
    }
}