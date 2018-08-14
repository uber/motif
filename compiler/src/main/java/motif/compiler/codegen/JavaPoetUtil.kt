/*
 * Copyright (c) 2018 Uber Technologies, Inc.
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

import com.squareup.javapoet.*
import motif.compiler.javax.Executable
import motif.compiler.javax.ExecutableParam
import motif.compiler.javax.JavaxUtil
import motif.ir.graph.Scope
import motif.ir.source.accessmethod.AccessMethod
import motif.ir.source.base.Dependency
import motif.ir.source.base.Type
import motif.ir.source.child.ChildMethod
import motif.ir.source.dependencies.RequiredDependencies
import motif.ir.source.dependencies.RequiredDependency
import motif.ir.source.objects.FactoryMethod
import motif.ir.source.objects.ObjectsClass
import motif.ir.source.objects.SpreadMethod
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror

interface JavaPoetUtil : JavaxUtil {

    val Type.mirror: TypeMirror
        get() = userData as TypeMirror

    val ObjectsClass.isInterface: Boolean
        get() = type.asElement().kind == ElementKind.INTERFACE

    fun ObjectsClass.abstractFactoryMethods(): List<Executable> {
        return factoryMethods
                .filter { it.isAbstract }
                .map { it.executable }
    }

    val SpreadMethod.executable: Executable
        get() = userData as Executable

    val FactoryMethod.executable: Executable
        get() = userData as Executable

    fun Executable.overriding(): MethodSpec.Builder {
        return MethodSpec.overriding(element, owner, env.typeUtils)
    }

    fun Executable.overrideUnsupported(): MethodSpec {
        return overriding()
                .addStatement("throw new \$T()", UnsupportedOperationException::class.java)
                .build()
    }

    val AccessMethod.executable: Executable
        get() = userData as Executable

    fun Executable.overrideWithFinalParams(): MethodSpec.Builder {
        val builder = MethodSpec.methodBuilder(name)
                .addAnnotation(Override::class.java)
                .returns(returnType.typeName)

        parameters
                .map {
                    it.specBuilder()
                            .addModifiers(Modifier.FINAL)
                            .build()
                }
                .forEach { builder.addParameter(it) }

        return builder
    }

    fun ExecutableParam.specBuilder(): ParameterSpec.Builder {
        return ParameterSpec.builder(
                type.typeName,
                element.simpleName.toString())
    }

    val ObjectsClass.type: DeclaredType
        get() = userData as DeclaredType

    val ChildMethod.executable: Executable
        get() = userData as Executable

    val Scope.type: DeclaredType
        get() = scopeClass.userData as DeclaredType

    val motif.ir.source.base.Annotation.mirror: AnnotationMirror
        get() = userData as AnnotationMirror

    val Dependency.typeMirror: TypeMirror
        get() = userData as TypeMirror

    val Dependency.typeName: TypeName
        get() = ClassName.get(typeMirror)

    fun TypeSpec.write(packageName: String): TypeSpec {
        JavaFile.builder(packageName, this).build().writeTo(env.filer)
        return this
    }

    fun RequiredDependencies.methodSpecBuilders(): Map<Dependency, MethodSpec.Builder> {
        return nameScope {
            list.associateBy({ it.dependency }) { it.dependency.methodSpecBuilder() }
        }
    }

    fun <T> nameScope(block: NameScope.() -> T): T {
        return NameScope(env).block()
    }

    class NameScope(override val env: ProcessingEnvironment) : JavaPoetUtil {

        private val names = UniqueNameSet()

        fun Dependency.methodSpecBuilder(): MethodSpec.Builder {
            return MethodSpec.methodBuilder(name())
                    .returns(typeName)
                    .apply { qualifier?.let { addAnnotation(it.spec()) } }
        }

        fun RequiredDependency.parameterSpec(): ParameterSpec {
            // TODO handle Provider
            return ParameterSpec.builder(dependency.typeName, dependency.name())
                    .apply { dependency.qualifier?.let { addAnnotation(it.spec()) } }
                    .build()
        }

        fun motif.ir.source.base.Annotation.spec(): AnnotationSpec {
            return AnnotationSpec.get(mirror)
        }

        fun Dependency.name(): String {
            return names.unique(Names.safeName(typeMirror))
        }

        fun claim(name: String) {
            names.claim(name)
        }
    }
}