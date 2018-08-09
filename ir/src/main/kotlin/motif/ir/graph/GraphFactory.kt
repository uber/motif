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
        } catch (e: ScopeCycleError) {
            Graph(mapOf(), mapOf(), e)
        }
    }

    private fun createUnsafe(): Graph {
        val nodes = scopeClasses.values.associateBy({ it }) { node(listOf(), it.type) }
        return Graph(this.nodes, nodes, null)
    }

    private fun node(visited: List<Type>, scopeType: Type): Node {
        if (scopeType in visited) {
            throw ScopeCycleError(visited)
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

    companion object {

        fun create(sourceSet: SourceSet): Graph {
            return GraphFactory(sourceSet).create()
        }
    }
}