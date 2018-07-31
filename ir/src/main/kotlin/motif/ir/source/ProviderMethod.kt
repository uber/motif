package motif.ir.source

import motif.ir.graph.Dependency

interface ProviderMethod : Source {

    val provided: Dependency
    val parameters: List<Parameter>
}