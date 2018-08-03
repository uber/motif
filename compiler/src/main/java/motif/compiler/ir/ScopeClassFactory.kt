package motif.compiler.ir

import motif.compiler.javax.JavaxUtil
import motif.compiler.ir
import motif.ir.source.ScopeClass
import motif.ir.source.accessmethod.AccessMethod
import motif.ir.source.child.ChildDeclaration
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.type.DeclaredType

class ScopeClassFactory(override val env: ProcessingEnvironment) : JavaxUtil {

    private val childFactory = ChildDeclarationFactory(env)
    private val accessMethodFactory = AccessMethodFactory(env)
    private val objectsClassFactory = ObjectsImplFactory(env)
    private val explicitDependenciesFactory = ExplicitDependenciesFactory(env)

    fun create(scopeType: DeclaredType): ScopeClass {
        val childDeclarations: MutableList<ChildDeclaration> = mutableListOf()
        val accessMethods: MutableList<AccessMethod> = mutableListOf()

        scopeType.methods()
                .forEach {
                    when {
                        childFactory.isApplicable(it) -> childDeclarations.add(childFactory.create(it))
                        accessMethodFactory.isApplicable(it) -> accessMethods.add(accessMethodFactory.create(it))
                        else -> throw RuntimeException("Invalid Scope method: $it")
                    }
                }

        val objectsClass = objectsClassFactory.create(scopeType)
        val explicitDependencies = explicitDependenciesFactory.create(scopeType)

        return ScopeClass(
                scopeType,
                scopeType.ir,
                childDeclarations,
                accessMethods,
                objectsClass,
                explicitDependencies)
    }
}