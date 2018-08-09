package motif.compiler.ir

import motif.compiler.errors.CompilerError
import motif.compiler.javax.JavaxUtil
import motif.compiler.ir
import motif.ir.source.ScopeClass
import motif.ir.source.accessmethod.AccessMethod
import motif.ir.source.child.ChildMethod
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.type.DeclaredType

class ScopeClassFactory(override val env: ProcessingEnvironment) : JavaxUtil {

    private val childFactory = ChildDeclarationFactory(env)
    private val accessMethodFactory = AccessMethodFactory(env)
    private val objectsClassFactory = ObjectsImplFactory(env)
    private val explicitDependenciesFactory = ExplicitDependenciesFactory(env)

    fun create(scopeType: DeclaredType): ScopeClass {
        val childMethods: MutableList<ChildMethod> = mutableListOf()
        val accessMethods: MutableList<AccessMethod> = mutableListOf()

        scopeType.methods()
                .forEach {
                    when {
                        childFactory.isApplicable(it) -> childMethods.add(childFactory.create(it))
                        accessMethodFactory.isApplicable(it) -> accessMethods.add(accessMethodFactory.create(it))
                        else -> throw CompilerError(it.element, "Invalid Scope method")
                    }
                }

        val objectsClass = objectsClassFactory.create(scopeType)
        val explicitDependencies = explicitDependenciesFactory.create(scopeType)

        return ScopeClass(
                scopeType,
                scopeType.ir,
                childMethods,
                accessMethods,
                objectsClass,
                explicitDependencies)
    }
}