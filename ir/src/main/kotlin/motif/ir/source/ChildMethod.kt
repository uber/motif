package motif.ir.source

interface ChildMethod : Source {

    val parameters: List<Parameter>

    override val type: SourceType
        get() = SourceType.CHILD_METHOD
}