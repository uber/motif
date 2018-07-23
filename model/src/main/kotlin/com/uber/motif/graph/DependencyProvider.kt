package com.uber.motif.graph

import com.uber.motif.source.Source

class DependencyProvider(
        val source: Source,
        val dependency: Dependency) {

    val consumers: MutableSet<DependencyConsumer> = mutableSetOf()
}