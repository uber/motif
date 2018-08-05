package motif.ir.graph

import motif.ir.source.dependencies.Dependencies
import motif.ir.source.dependencies.GeneratedDependencies

class GeneratedNode(private val generatedDependencies: GeneratedDependencies) : Node {

    override val childDependencies: Dependencies = Dependencies(listOf())

    override val dependencies: Dependencies by lazy {
        Dependencies(generatedDependencies.annotatedDependencies)
    }

    override val missingDependencies: Dependencies? = null

    override val dependencyCycle: DependencyCycle? = null

    override val duplicateFactoryMethods: List<DuplicateFactoryMethod> = listOf()

    override val children: List<Node> = listOf()

    override val parents: MutableList<ScopeClassNode> = mutableListOf()
}