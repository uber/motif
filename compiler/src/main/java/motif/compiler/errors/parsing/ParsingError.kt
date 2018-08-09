package motif.compiler.errors.parsing

import javax.lang.model.element.Element

// TODO Make this a sealed class. Wherever we throw a ParsingError, convert to a subclass of this
// class. For instance, we throw a ParsingError when we find multiple qualifiers on the same method.
// We should instead throw a more specific subclass "MultipleQualifiersError : ParsingError". We can
// then add tests for these cases in the same way we test for GraphValidationErrors.
class ParsingError(val element: Element, override val message: String) : RuntimeException(message)