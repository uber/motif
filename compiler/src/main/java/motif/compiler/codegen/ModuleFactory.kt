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
package motif.compiler.codegen

import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import dagger.Module
import dagger.Provides
import motif.internal.DaggerScope
import motif.models.graph.Scope
import motif.models.motif.dependencies.Dependency
import motif.models.motif.objects.FactoryMethod
import motif.models.motif.objects.ObjectsClass
import motif.models.motif.objects.SpreadMethod
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Modifier

class ModuleFactory(
        env: ProcessingEnvironment,
        cacheScope: CacheScope) : CodegenCache(env, cacheScope) {

    fun create(scope: Scope): TypeSpec {
        val builder = TypeSpec.classBuilder(scope.moduleTypeName)
                .addAnnotation(Module::class.java)
                .addModifiers(Modifier.STATIC, Modifier.ABSTRACT)
        val objectsClass = scope.objectsClass ?: return builder.build()
        val objectsField = FieldSpec.builder(objectsClass.typeName, "objects", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("new \$T()", scope.objectsImplTypeName)
                .build()
        builder.addField(objectsField)
        builder.build(objectsClass, objectsField)
        return builder.build()
    }

    private fun TypeSpec.Builder.build(
            objectsClass: ObjectsClass,
            objectsField: FieldSpec) {
        nameScope {
            fun Dependency.providerMethod(isCached: Boolean): MethodSpec.Builder {
                return methodSpecBuilder()
                        .addAnnotation(Provides::class.java)
                        .addModifiers(Modifier.STATIC)
                        .apply { if (isCached) addAnnotation(DaggerScope::class.java) }
            }

            val methods = objectsClass.factoryMethods
                    .flatMap { factoryMethod ->
                        val spec = factoryMethod.providedDependency
                                .providerMethod(factoryMethod.isCached)
                                .build(objectsField, factoryMethod)
                        val spreadSpecs = factoryMethod.spreadDependency?.methods?.map { spreadMethod ->
                            spreadMethod.dependency
                                    .providerMethod(factoryMethod.isCached)
                                    .build(spreadMethod)
                        } ?: listOf()
                        spreadSpecs + spec
                    }
            addMethods(methods)
        }
    }

    private fun MethodSpec.Builder.build(objectsField: FieldSpec, method: FactoryMethod): MethodSpec {
        val dependency = method.providedDependency
        val parameters = nameScope {
            method.requiredDependencies.list.map { it.parameterSpec() }
        }.apply { addParameters(this) }.toTypedArray()
        val callParams: String = parameters.joinToString(", ") { "\$N" }

        when (method.kind) {
            FactoryMethod.Kind.BASIC -> {
                if (method.isStatic) {
                    addStatement("return \$T.\$N($callParams)", objectsField.type, method.cir.name, *parameters)
                } else {
                    addStatement("return \$N.\$N($callParams)", objectsField, method.cir.name, *parameters)
                }
            }
            FactoryMethod.Kind.BINDS -> addStatement("return $callParams", *parameters)
            FactoryMethod.Kind.CONSTRUCTOR -> addStatement("return new \$T($callParams)", dependency.typeName, *parameters)
        }

        return build()
    }

    private fun MethodSpec.Builder.build(method: SpreadMethod): MethodSpec {
        val spreadParameter = method.source.parameterSpec("source")
        addParameter(spreadParameter)
        addStatement("return \$N.\$N()", spreadParameter, method.cir.name)
        return build()
    }
}
