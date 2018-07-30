package motif.source

interface DependenciesInterface : Source {

    val methods: List<DependenciesMethod>

    override val type: SourceType
        get() = SourceType.DEPENDENCIES_INTERFACE
}