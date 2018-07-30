package motif.source

import motif.graph.Dependency

interface DependenciesMethod : Source {

    val dependency: Dependency

    override val type: SourceType
        get() = SourceType.SCOPE_DEPENDENCY_METHOD
}