package motif.graph

import motif.source.Source

class DependencyProvider(
        val source: Source,
        val dependency: Dependency) {

    val consumers: MutableSet<DependencyConsumer> = mutableSetOf()
}