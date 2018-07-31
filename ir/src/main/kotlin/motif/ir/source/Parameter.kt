package motif.ir.source

import motif.ir.graph.Dependency

interface Parameter : Source {

    val dependency: Dependency
}