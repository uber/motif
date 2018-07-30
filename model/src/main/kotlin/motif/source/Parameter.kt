package motif.source

import motif.graph.Dependency

interface Parameter : Source {

    val dependency: Dependency
}