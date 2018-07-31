package motif.ir.graph

import motif.ir.source.Source

class DependencyProvider(
        val source: Source,
        val dependency: Dependency) {

    val consumers: MutableSet<DependencyConsumer> = mutableSetOf()
}