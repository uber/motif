package motif.ir.source

interface ChildImplementation : Source {

    val dependenciesInterface: DependenciesInterface

    override val type: SourceType
        get() = SourceType.CHILD_IMPLEMENTATION
}