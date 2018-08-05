package motif.ir.graph

import motif.ir.graph.errors.ScopeCycleError
import motif.ir.graph.errors.UnprocessedScopeError
import motif.ir.source.ScopeClass
import motif.ir.source.SourceSet
import motif.ir.source.base.Type
import motif.ir.source.dependencies.GeneratedDependencies

class GraphFactory private constructor(sourceSet: SourceSet) {

    private val scopeClasses: Map<Type, ScopeClass> = sourceSet.scopeClasses.associateBy { it.type }

    private val nodes: MutableMap<Type, Node> = mutableMapOf()

    private fun create(): Graph {
        return try {
            createUnsafe()
        } catch (e: ScopeCycleError) {
            Graph(mapOf(), mapOf(), e, null)
        } catch (e: UnprocessedScopeError) {
            Graph(mapOf(), mapOf(), null, e)
        }
    }

    private fun createUnsafe(): Graph {
        val nodes = scopeClasses.values.associateBy({ it }) { calculatedNode(listOf(), it.type) }
        return Graph(this.nodes, nodes, null, null)
    }

    private fun calculatedNode(visited: List<Type>, scopeType: Type): Node {
        if (scopeType in visited) {
            throw ScopeCycleError(visited)
        }
        val newVisited = visited + scopeType
        return nodes.computeIfAbsent(scopeType) {
            val scopeClass = scopeClasses[scopeType] ?: throw UnprocessedScopeError(scopeType)
            val scopeChildren: List<ScopeChild> = scopeClass.childDeclarations
                    .map { childDeclaration ->
                        val method = childDeclaration.method
                        childDeclaration.generatedDependencies?.let {
                            ScopeChild(method, generatedNode(method.scope, it))
                        } ?: ScopeChild(method, calculatedNode(newVisited, method.scope))
                    }
            ScopeClassNode(scopeClass, scopeChildren).apply {
                scopeChildren.forEach { it.node.parents.add(this) }
            }
        }
    }

    private fun generatedNode(scopeType: Type, generatedDependencies: GeneratedDependencies): Node {
        return nodes.computeIfAbsent(scopeType) { GeneratedNode(generatedDependencies)  }
    }

    companion object {

        fun create(sourceSet: SourceSet): Graph {
            return GraphFactory(sourceSet).create()
        }
    }
}