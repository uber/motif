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
import com.squareup.javapoet.TypeSpec
import dagger.BindsInstance
import dagger.Component
import javax.lang.model.element.Modifier

class ComponentBuilder private constructor(
        val spec: TypeSpec,
        val typeName: ClassName,
        val dependenciesMethod: MethodSpec,
        val scopeMethod: MethodSpec,
        val buildMethod: MethodSpec) {

    companion object {

        fun create(
                dependencies: Dependencies,
                scopeTypeName: ClassName,
                componentTypeName: ClassName): ComponentBuilder {
            val typeName = componentTypeName.nestedClass("Builder")
            val typeSpec = TypeSpec.interfaceBuilder(typeName)
            typeSpec.addAnnotation(Component.Builder::class.java)
            typeSpec.addModifiers(Modifier.PUBLIC, Modifier.STATIC)

            val dependenciesMethod = MethodSpec.methodBuilder("dependencies")
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .returns(typeName)
                    .addParameter(dependencies.typeName, "dependencies")
                    .build()

            val scopeMethod = MethodSpec.methodBuilder("scope")
                    .addAnnotation(BindsInstance::class.java)
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .returns(typeName)
                    .addParameter(scopeTypeName, "scope")
                    .build()

            val buildMethod = MethodSpec.methodBuilder("build")
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .returns(componentTypeName)
                    .build()

            typeSpec.addMethods(listOf(
                    dependenciesMethod,
                    scopeMethod,
                    buildMethod
            ))

            return ComponentBuilder(typeSpec.build(), typeName, dependenciesMethod, scopeMethod, buildMethod)
        }
    }
}
