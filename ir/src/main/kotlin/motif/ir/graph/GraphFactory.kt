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
package motif.ir.graph

import motif.ir.graph.errors.ScopeCycleError
import motif.ir.source.ScopeClass
import motif.ir.source.SourceSet
import motif.ir.source.base.Type

class GraphFactory private constructor(sourceSet: SourceSet) {

    private val scopeClasses: Map<Type, ScopeClass> = sourceSet.scopeClasses.associateBy { it.type }

    private val nodes: MutableMap<Type, Node> = mutableMapOf()

    private fun create(): Graph {
        return try {
            createUnsafe()
        } catch (e: ScopeCycleException) {
            Graph(mapOf(), e.error)
        }
    }

    private fun createUnsafe(): Graph {
        val nodes = scopeClasses.values.associateBy({ it.type }) { node(listOf(), it.type) }
        return Graph(nodes, null)
    }

    private fun node(visited: List<Type>, scopeType: Type): Node {
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

        fun create(sourceSet: SourceSet): Graph {
            return GraphFactory(sourceSet).create()
        }
    }
}