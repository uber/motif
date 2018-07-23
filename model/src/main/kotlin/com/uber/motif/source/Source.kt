package com.uber.motif.source

interface Source {

    val parent: Source?

    val id: String

    val type: SourceType
}