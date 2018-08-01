package motif.ir.source

import motif.ir.source.accessmethod.AccessMethod
import motif.ir.source.base.Dependency
import motif.ir.source.base.Type
import motif.ir.source.child.ChildDeclaration
import motif.ir.source.dependencies.AnnotatedDependency
import motif.ir.source.dependencies.Dependencies
import motif.ir.source.dependencies.ExplicitDependencies
import motif.ir.source.objects.FactoryMethod
import motif.ir.source.objects.ObjectsClass

class ScopeClass(
        val userData: Any?,
        val type: Type,
        val childDeclarations: List<ChildDeclaration>,
        val accessMethods: List<AccessMethod>,
        val objectsClass: ObjectsClass?,
        val explicitDependencies: ExplicitDependencies?) {

    val scopeDependency = Dependency(userData, type, null)

    private val factoryMethods: List<FactoryMethod> = objectsClass?.factoryMethods ?: listOf()
    private val allProvided: List<Dependency> = factoryMethods.flatMap { it.providedDependencies } + scopeDependency
    private val consumed: List<Dependency> = factoryMethods.flatMap { it.consumedDependencies } + accessMethods.map { it.dependency }

    val exposed: List<Dependency> = factoryMethods.flatMap { it.exposedDependencies }

    val selfDependencies: Dependencies by lazy {
        val annotatedDependencies = (consumed - allProvided).map { AnnotatedDependency(it, false, setOf(type)) }
        Dependencies(annotatedDependencies)
    }

    override fun toString(): String {
        return type.toString()
    }
}