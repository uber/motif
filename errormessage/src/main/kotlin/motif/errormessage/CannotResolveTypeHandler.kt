package motif.errormessage

import motif.models.CannotResolveType

class CannotResolveTypeHandler(private val error: CannotResolveType) : ErrorHandler {

    override val name = "CANNOT RESOLVE TYPE"

    override fun StringBuilder.handle() {
        appendLine("""
            Following type cannot be resolved:

              ${error.type.qualifiedName}
        """.trimIndent())
    }

}