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
import motif.ast.IrClass
import motif.ast.IrMethod
import motif.ast.IrType
import motif.core.ResolvedGraph
import motif.models.*
import java.util.*
import javax.lang.model.element.Modifier

class Dependencies private constructor(
        val spec: TypeSpec,
        val typeName: TypeName,
        private val methodSpecs: SortedMap<Type, Method>) {

    private val methodList = methodSpecs.values.toList()

    fun getMethodSpec(type: Type): MethodSpec? {
        return methodSpecs[type]?.methodSpec
    }

    fun isEmpty(): Boolean {
        return methodSpecs.isEmpty()
    }

    fun types(): List<Type> {
        return methodSpecs.keys.toList()
    }

    fun getMethods(): List<Method> {
        return methodList
    }

    class Method(val type: Type, val methodSpec: MethodSpec)

    companion object {

        fun create(
                graph: ResolvedGraph,
                scope: Scope,
                scopeImplTypeName: ClassName): Dependencies {
            val sinks = graph.getUnsatisfied(scope)
            val nameScope = NameScope()
            val typeName = scopeImplTypeName.nestedClass("Dependencies")

            val methods: SortedMap<Type, Method> = sinks
                    .map { (type, sinks) ->
                        val methodSpec = methodSpec(nameScope, type)
                                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                                .addJavadoc(getJavadoc(sinks))
                                .build()
                        Method(type, methodSpec)
                    }
                    .associateBy { it.type }
                    .toSortedMap()

            val typeSpec = TypeSpec.interfaceBuilder(typeName)
                    .addModifiers(Modifier.PUBLIC)
                    .addMethods(methods.values.map { it.methodSpec })
                    .build()

            return Dependencies(typeSpec, typeName, methods)
        }

        private fun getJavadoc(sinks: List<Sink>): CodeBlock {
            val sinkList = sinks.map { sink ->
                val (callerType, callerMethod) = when (sink) {
                    is FactoryMethodSink -> Pair(sink.parameter.owner.type, sink.parameter.method)
                    is AccessMethodSink -> Pair(sink.scope.clazz.type, sink.accessMethod.method)
                }

                CodeBlock.builder()
                        .add("<li>{@link \$L#", removeGenericSignature(callerType.qualifiedName))
                        .add(getMethodReference(callerType, callerMethod))
                        .add("</li>")
                        .build()
            }

            return CodeBlock
                    .builder()
                    .add("<ul>\nRequested from:\n")
                    .add(CodeBlock.join(sinkList, "\n"))
                    .add("\n</ul>\n")
                    .build()
        }

        private fun getMethodReference(callerType: IrType, callerMethod: IrMethod): CodeBlock {
            val methodName = if (callerMethod.isConstructor) callerType.simpleName else callerMethod.name

            val paramList = callerMethod
                    .parameters
                    .map { removeGenericSignature(it.type.qualifiedName) }
                    .joinToString()

            return CodeBlock.of("\$N(\$L)", methodName, paramList)
        }

        private fun removeGenericSignature(name: String): String {
            return name.takeWhile { it != '<' }
        }
    }
}
