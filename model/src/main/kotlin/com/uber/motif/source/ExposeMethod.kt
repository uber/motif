package com.uber.motif.source

import com.uber.motif.graph.Dependency

interface ExposeMethod : Source {

    val exposed: Dependency

    override val type: SourceType
        get() = SourceType.EXPOSE_METHOD
}