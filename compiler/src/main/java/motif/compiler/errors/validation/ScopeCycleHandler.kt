package motif.compiler.errors.validation

import motif.ir.graph.errors.ScopeCycleError
import javax.lang.model.element.Element

class ScopeCycleHandler : ErrorHandler<ScopeCycleError>() {

    override fun message(error: ScopeCycleError): String {
        return this::class.java.name
    }

    override fun element(error: ScopeCycleError): Element? {
        return null
    }
}