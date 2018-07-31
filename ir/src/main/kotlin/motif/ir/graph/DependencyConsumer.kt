package motif.ir.graph

import motif.ir.source.Source

class DependencyConsumer(
        val source: Source,
        val dependency: Dependency) {

    var providers: MutableSet<DependencyProvider> = mutableSetOf()
}