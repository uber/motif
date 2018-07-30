package motif.graph

import motif.source.Source

class DependencyConsumer(
        val source: Source,
        val dependency: Dependency) {

    var providers: MutableSet<DependencyProvider> = mutableSetOf()
}