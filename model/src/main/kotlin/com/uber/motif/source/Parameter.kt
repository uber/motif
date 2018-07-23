package com.uber.motif.source

import com.uber.motif.graph.Dependency

interface Parameter : Source {

    val dependency: Dependency
}