package motif.source

import motif.graph.Dependency

interface ProviderMethod : Source {

    val provided: Dependency
    val parameters: List<Parameter>
}