package motif.source

interface ScopeClass : Source {

    override val type: SourceType
        get() = SourceType.SCOPE_CLASS

    val exposeMethods: List<ExposeMethod>
    val childDeclarations: List<ChildDeclaration>
    val objectClass: ObjectClass?
}