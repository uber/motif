package motif.ir.source

import motif.ir.graph.Dependency

interface ExposeMethod : Source {

    val exposed: Dependency

    override val type: SourceType
        get() = SourceType.EXPOSE_METHOD
}