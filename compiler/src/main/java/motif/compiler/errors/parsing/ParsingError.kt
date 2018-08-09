package motif.compiler.errors.parsing

import javax.lang.model.element.Element

class ParsingError(val element: Element, override val message: String) : RuntimeException(message)