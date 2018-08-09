package motif.compiler.errors

import javax.lang.model.element.Element

class CompilerError(val element: Element, override val message: String) : RuntimeException(message)