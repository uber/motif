package com.uber.motif.source

import com.uber.motif.graph.Dependency

interface DependenciesMethod : Source {

    val dependency: Dependency

    override val type: SourceType
        get() = SourceType.SCOPE_DEPENDENCY_METHOD
}