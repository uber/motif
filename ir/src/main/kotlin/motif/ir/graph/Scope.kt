package motif.ir.graph

import motif.ir.source.ScopeClass
import motif.ir.source.accessmethod.AccessMethod
import motif.ir.source.base.Dependency
import motif.ir.source.child.ChildDeclaration
import motif.ir.source.dependencies.Dependencies
import motif.ir.source.objects.ObjectsClass

class Scope(
        val scopeClass: ScopeClass,
        val childDependencies: Dependencies,
        val dependencies: Dependencies) {

    val childDeclarations: List<ChildDeclaration> = scopeClass.childDeclarations
    val accessMethods: List<AccessMethod> = scopeClass.accessMethods
    val objectsClass: ObjectsClass? = scopeClass.objectsClass
    val scopeDependency: Dependency = scopeClass.scopeDependency
}