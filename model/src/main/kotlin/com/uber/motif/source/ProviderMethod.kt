package com.uber.motif.source

import com.uber.motif.graph.Dependency

interface ProviderMethod : Source {

    val provided: Dependency
    val parameters: List<Parameter>
}