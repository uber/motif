package motif.compiler.errors

open class CompilationError(override val message: String) : RuntimeException(message)