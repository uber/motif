package motif.ir.source

interface Source {

    val parent: Source?

    val id: String

    val type: SourceType
}