package motif.models.errors

import motif.models.graph.Node
import motif.models.java.IrClass
import motif.models.java.IrMethod
import motif.models.java.IrParameter
import motif.models.java.IrType
import motif.models.motif.ScopeClass
import motif.models.motif.child.ChildMethod
import motif.models.motif.dependencies.Dependency
import motif.models.motif.dependencies.RequiredDependency
import motif.models.motif.objects.FactoryMethod

sealed class MotifError : RuntimeException()

// Parsing Errors

class ScopeMustBeAnInterface(val scopeClass: IrClass) : MotifError()
class InvalidScopeMethod(val method: IrMethod) : MotifError()
class ObjectsFieldFound(val objectsClass: IrClass) : MotifError()
class ObjectsConstructorFound(val objectsClass: IrClass) : MotifError()
class VoidObjectsMethod(val method: IrMethod) : MotifError()
class NullableFactoryMethod(val method: IrMethod) : MotifError()
class NullableDependency(val parameter: IrParameter, val method: IrMethod) : MotifError()
class InvalidObjectsMethod(val method: IrMethod) : MotifError()
class TypeNotSpreadable(val type: IrType, val method: IrMethod) : MotifError()
class NoSuitableConstructor(val type: IrType, val method: IrMethod) : MotifError()
class NotAssignableBindsMethod(val method: IrMethod, val returnType: IrType, val parameterType: IrType) : MotifError()
class VoidDependenciesMethod(val method: IrMethod) : MotifError()
class DependencyMethodWithParameters(val method: IrMethod) : MotifError()
class MissingInjectAnnotation(val type: IrType, val method: IrMethod) : MotifError()

// Graph Validation Errors

class DependencyCycleError(val scopeClass: ScopeClass, val cycle: List<FactoryMethod>) : MotifError()
class DuplicateFactoryMethodsError(val duplicate: FactoryMethod, val existing: Set<IrType>) : MotifError()
class MissingDependenciesError(val requiredBy: Node, val dependencies: List<Dependency>) : MotifError()
class ScopeCycleError(val cycle: List<IrType>) : MotifError()

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
        val requiredDependency: RequiredDependency) : MotifError()

/**
 * Similar to NotExposedError except applied to dynamic dependencies.
 */
class NotExposedDynamicError(
        val scopeClass: ScopeClass,
        val childMethod: ChildMethod,
        val requiredDependency: RequiredDependency) : MotifError()