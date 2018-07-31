package motif.ir.source

import motif.ir.graph.Dependency

interface DependenciesMethod : Source {

    val dependency: Dependency

    override val type: SourceType
        get() = SourceType.DEPENDENCIES_METHOD
}