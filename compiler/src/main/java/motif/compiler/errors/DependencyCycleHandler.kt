package motif.compiler.errors

import motif.ir.graph.errors.DependencyCycleError
import motif.ir.graph.errors.ScopeCycleError
import javax.lang.model.element.Element

class DependencyCycleHandler : ErrorHandler<DependencyCycleError>() {

    override fun message(error: DependencyCycleError): String {
        return this::class.java.name
    }

    override fun element(error: DependencyCycleError): Element? {
        return null
    }
}