package motif.compiler.errors

import motif.ir.graph.errors.*
import javax.lang.model.element.Element

abstract class ErrorHandler<T : GraphError> {

    abstract fun message(error: T): String
    abstract fun element(error: T): Element?

    fun error(error: T): ErrorMessage {
        return ErrorMessage(element(error), message(error))
    }

    companion object {

        fun handle(error: GraphError): ErrorMessage {
            return when (error) {
                is MissingDependenciesError -> MissingDependenciesHandler().error(error)
                is ScopeCycleError -> ScopeCycleHandler().error(error)
                is DependencyCycleError -> DependencyCycleHandler().error(error)
                is DuplicateFactoryMethodsError -> DuplicateFactoryMethodsHandler().error(error)
            }
        }
    }
}