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
package motif.models.graph

import motif.models.errors.MotifError
import motif.models.errors.ScopeCycleError
import motif.ast.IrType
import motif.models.motif.ScopeClass
import motif.models.motif.SourceSet
import motif.models.parsing.SourceSetParser

class GraphFactory private constructor(sourceSet: SourceSet) {

    private val scopeClasses: Map<IrType, ScopeClass> = sourceSet.scopeClasses.associateBy { it.ir.type }

    private val nodes: MutableMap<IrType, Node> = mutableMapOf()

    private fun create(): Graph {
        return try {
            createUnsafe()
        } catch (e: ScopeCycleException) {
            Graph(mapOf(), listOf(), e.error)
        }
    }

    private fun createUnsafe(): Graph {
        val nodes = scopeClasses.values.associateBy({ it.ir.type }) { node(listOf(), it.ir.type) }
        return Graph(nodes, listOf(), null)
    }

    private fun node(visited: List<IrType>, scopeType: IrType): Node {
        if (scopeType in visited) {
            throw ScopeCycleException(ScopeCycleError(visited))
        }
        val newVisited = visited + scopeType
        return nodes.computeIfAbsent(scopeType) {
            val scopeClass = scopeClasses[scopeType] ?: throw IllegalStateException()
            val scopeChildren: List<ScopeChild> = scopeClass.childMethods
                    .map { childMethod ->
                        ScopeChild(childMethod, node(newVisited, childMethod.scope))
                    }
            Node(scopeClass, scopeChildren).apply {
                scopeChildren.forEach { it.node.parents.add(this) }
            }
        }
    }

    private class ScopeCycleException(val error: ScopeCycleError) : RuntimeException()

    companion object {

        fun create(scopeAnnotatedTypes: Set<IrType>): Graph {
            val sourceSet = try {
                SourceSetParser().parse(scopeAnnotatedTypes)
            } catch (e : MotifError) {
                return Graph(mapOf(), listOf(e), null)
            }
            return GraphFactory(sourceSet).create()
        }
    }
}
