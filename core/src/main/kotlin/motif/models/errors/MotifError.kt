package motif.models.errors

import motif.models.graph.Node
import motif.ast.IrClass
import motif.ast.IrMethod
import motif.ast.IrParameter
import motif.ast.IrType
import motif.models.motif.ScopeClass
import motif.models.motif.child.ChildMethod
import motif.models.motif.dependencies.Dependency
import motif.models.motif.dependencies.RequiredDependency
import motif.models.motif.objects.FactoryMethod

sealed class MotifError : RuntimeException() {

    abstract val debugString: String
}

// Parsing Errors

class ScopeMustBeAnInterface(val scopeClass: IrClass) : MotifError() {

    override val debugString = "Scope must be an interface: ${scopeClass.qualifiedName}"
}

class InvalidScopeMethod(val scopeClass: IrClass, val method: IrMethod) : MotifError() {

    override val debugString = "Scope method is invalid: ${scopeClass.qualifiedName}.${method.name}"
}

class ObjectsFieldFound(val objectsClass: IrClass) : MotifError() {

    override val debugString = "Objects class may not have fields: ${objectsClass.qualifiedName}"
}

class ObjectsConstructorFound(val objectsClass: IrClass) : MotifError() {

    override val debugString = "Objects class may not define constructors: ${objectsClass.qualifiedName}"
}

class VoidObjectsMethod(val objectsClass: IrClass, val method: IrMethod) : MotifError() {

    override val debugString = "Objects methods must be non-void: ${objectsClass.qualifiedName}.${method.name}"
}

class NullableFactoryMethod(val objectsClass: IrClass, val method: IrMethod) : MotifError() {

    override val debugString = "Factory method may not be nullable: ${objectsClass.qualifiedName}.${method.name}"
}

class NullableDependency(val owner: IrClass, val method: IrMethod, val parameter: IrParameter) : MotifError() {

    override val debugString = "Parameter may not be nullable: ${parameter.name} in ${owner.qualifiedName}.${method.name}"
}

class InvalidObjectsMethod(val objectsClass: IrClass, val method: IrMethod) : MotifError() {

    override val debugString = "Objects method is invalid: ${objectsClass.qualifiedName}.${method.name}"
}

class TypeNotSpreadable(val objectsClass: IrClass, val method: IrMethod, val type: IrType) : MotifError() {

    override val debugString = "Type is not spreadable: ${type.qualifiedName} at ${objectsClass.qualifiedName}.${method.name}"
}

class NoSuitableConstructor(val type: IrType, val method: IrMethod) : MotifError() {

    override val debugString = javaClass.name
}

class NotAssignableBindsMethod(val objectsClass: IrClass, val method: IrMethod, val returnType: IrType, val parameterType: IrType) : MotifError() {

    override val debugString = "Invalid binds method: ${objectsClass.qualifiedName}.${method.name}"
}

class VoidDependenciesMethod(val method: IrMethod) : MotifError() {

    override val debugString = javaClass.name
}

class DependencyMethodWithParameters(val method: IrMethod) : MotifError() {

    override val debugString = javaClass.name
}

class MissingInjectAnnotation(val type: IrType, val method: IrMethod) : MotifError() {

    override val debugString = javaClass.name
}

// Graph Validation Errors

class DependencyCycleError(val scopeClass: ScopeClass, val cycle: List<FactoryMethod>) : MotifError() {

    override val debugString = javaClass.name
}

class DuplicateFactoryMethodsError(val duplicate: FactoryMethod, val existing: Set<IrType>) : MotifError() {

    override val debugString = javaClass.name
}

class MissingDependenciesError(val requiredBy: Node, val dependencies: List<Dependency>) : MotifError() {

    override val debugString = javaClass.name
}

class ScopeCycleError(val cycle: List<IrType>) : MotifError() {

    override val debugString = javaClass.name
}

/**
 * Compared to other GraphErrors, it's not as intuitive why NotExposedError needs to exist. We hit this error
 * when an ancestor scope defines a non-@Exposed factory method that provides a dependency required
 * by one of its descendants. Initially, it may seem like a premature failure since that dependency may be satisfied
 * by scopes higher in the graph. However, if a scope higher in the graph exposes a factory method that provides the
 * same type, the lower, non-@Exposed factory method would conflict, causing a DuplicateFactoryMethodsError. Thus,
 * there is no situation where this case is valid so we surface this error.
 */
class NotExposedError(
        val scopeClass: ScopeClass,
        val factoryMethod: FactoryMethod,
        val requiredDependency: RequiredDependency) : MotifError() {

    override val debugString = javaClass.name
}

/**
 * Similar to NotExposedError except applied to dynamic dependencies.
 */
class NotExposedDynamicError(
        val scopeClass: ScopeClass,
        val childMethod: ChildMethod,
        val requiredDependency: RequiredDependency) : MotifError() {

    override val debugString = javaClass.name
}