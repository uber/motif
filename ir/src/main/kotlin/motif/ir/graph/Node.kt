package motif.ir.graph

import motif.ir.source.dependencies.Dependencies

interface Node {
    val dependencies: Dependencies
    val childDependencies: Dependencies
    val missingDependencies: Dependencies?
    val dependencyCycle: DependencyCycle?
    val duplicateFactoryMethods: List<DuplicateFactoryMethod>
    val children: List<Node>

    // Mutable to allow circular reference between child <-> parent Nodes.
    // Should not be updated outside of GraphFactory.
    val parents: MutableList<ScopeClassNode>
}