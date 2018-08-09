package motif.compiler.errors

import motif.ir.graph.errors.DuplicateFactoryMethodsError
import javax.lang.model.element.Element

class DuplicateFactoryMethodsHandler : ErrorHandler<DuplicateFactoryMethodsError>() {

    override fun message(error: DuplicateFactoryMethodsError): String {
        return this::class.java.name
    }

    override fun element(error: DuplicateFactoryMethodsError): Element? {
        return null
    }
}