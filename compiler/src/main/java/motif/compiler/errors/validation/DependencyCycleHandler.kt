package motif.compiler.errors.validation

import motif.ir.graph.errors.DependencyCycleError
import javax.lang.model.element.Element

class DependencyCycleHandler : ErrorHandler<DependencyCycleError>() {

    override fun message(error: DependencyCycleError): String {
        return this::class.java.name
    }

    override fun element(error: DependencyCycleError): Element? {
        return null
    }
}