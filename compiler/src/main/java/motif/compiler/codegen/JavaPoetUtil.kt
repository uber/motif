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
import motif.compiler.ir.*
import motif.models.graph.Scope
import motif.models.java.IrAnnotation
import motif.models.java.IrClass
import motif.models.java.IrMethod
import motif.models.java.IrType
import motif.models.parsing.ParserUtil
import motif.models.motif.accessmethod.AccessMethod
import motif.models.motif.dependencies.Dependency
import motif.models.motif.child.ChildMethod
import motif.models.motif.dependencies.RequiredDependencies
import motif.models.motif.dependencies.RequiredDependency
import motif.models.motif.objects.FactoryMethod
import motif.models.motif.objects.ObjectsClass
import motif.models.motif.objects.SpreadMethod
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Modifier
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror

interface JavaPoetUtil : ParserUtil {

    val env: ProcessingEnvironment

    val IrType.cir: CompilerType
        get() = this as CompilerType

    val IrAnnotation.cir: CompilerAnnotation
        get() = this as CompilerAnnotation

    val IrClass.cir: CompilerClass
        get() = this as CompilerClass

    val IrMethod.cir: CompilerMethod
        get() = this as CompilerMethod

    val SpreadMethod.cir: CompilerMethod
        get() = ir.cir

    val FactoryMethod.cir: CompilerMethod
        get() = ir.cir

    val AccessMethod.cir: CompilerMethod
        get() = ir.cir

    val ObjectsClass.cir: CompilerType
        get() = type.cir

    val ChildMethod.cir: CompilerMethod
        get() = ir.cir

    val Scope.cir: CompilerClass
        get() = scopeClass.ir.cir

    val Dependency.cir: CompilerType
        get() = type.cir

    fun ClassName.qualifiedName(): String {
        return "${packageName()}.${simpleNames().joinToString(".")}"
    }

    fun ObjectsClass.abstractFactoryMethods(): List<CompilerMethod> {
        return factoryMethods
                .filter { it.isAbstract }
                .map { it.cir }
    }

    fun CompilerMethod.overriding(): MethodSpec.Builder {
        return MethodSpec.overriding(element, owner, env.typeUtils)
    }

    fun CompilerMethod.overrideUnsupported(): MethodSpec {
        return overriding()
                .addStatement("throw new \$T()", UnsupportedOperationException::class.java)
                .build()
    }

    fun CompilerMethod.overrideWithFinalParams(): MethodSpec.Builder {
        val builder = MethodSpec.methodBuilder(name)
                .addAnnotation(Override::class.java)
                .returns(returnType.cir.mirror.typeName)

        parameters
                .map {
                    it.specBuilder()
                            .addModifiers(Modifier.FINAL)
                            .build()
                }
                .forEach { builder.addParameter(it) }

        return builder
    }

    fun CompilerMethodParameter.specBuilder(): ParameterSpec.Builder {
        return ParameterSpec.builder(
                type.cir.mirror.typeName,
                element.simpleName.toString())
    }

    val Dependency.typeName: TypeName
        get() = ClassName.get(type.cir.mirror)

    fun TypeSpec.write(packageName: String): TypeSpec {
        JavaFile.builder(packageName, this).build().writeTo(env.filer)
        return this
    }

    fun RequiredDependencies.methodSpecBuilders(): Map<Dependency, MethodSpec.Builder> {
        return nameScope {
            list.associateBy({ it.dependency }) { it.dependency.methodSpecBuilder() }
        }
    }

    fun scopeImpl(scopeType: DeclaredType): ClassName {
        val scopeClassName = scopeType.typeName as ClassName
        val prefix = scopeClassName.simpleNames().joinToString("")
        return ClassName.get(scopeClassName.packageName(), "${prefix}Impl")
    }

    val TypeMirror.typeName: TypeName
        get() = ClassName.get(this)

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
            return ParameterSpec.builder(dependency.typeName, dependency.name())
                    .apply { dependency.qualifier?.let { addAnnotation(it.spec()) } }
                    .build()
        }

        fun IrAnnotation.spec(): AnnotationSpec {
            return AnnotationSpec.get(cir.mirror)
        }

        fun Dependency.name(): String {
            return names.unique(Names.safeName(type.cir.mirror))
        }

        fun claim(name: String) {
            names.claim(name)
        }
    }
}