package motif.ir.graph

import motif.ir.source.base.Dependency
import motif.ir.source.dependencies.Dependencies

interface Node {
    val dependencies: Dependencies
    val childDependencies: Dependencies
    val missingDependencies: Dependencies?
    val dependencyCycle: List<Dependency>?
    val children: List<Node>
}