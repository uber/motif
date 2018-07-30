package motif.source

interface ChildDeclaration : Source {

    val childScopeId: String
    val childMethod: ChildMethod
    val childImplementation: ChildImplementation?

    override val type: SourceType
        get() = SourceType.CHILD_DECLARATION
}