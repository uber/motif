package motif.compiler.ir

import motif.Scope
import motif.compiler.ir
import motif.compiler.javax.JavaxUtil
import motif.ir.source.ScopeClass
import motif.ir.source.SourceSet
import motif.ir.source.base.Type
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType

class SourceSetFactory(override val env: ProcessingEnvironment) : JavaxUtil {

    private val scopeClassFactory = ScopeClassFactory(env)

    fun create(roundEnv: RoundEnvironment): SourceSet {
        val scopes: MutableMap<Type, ScopeClass> = mutableMapOf()
        roundEnv.getElementsAnnotatedWith(Scope::class.java)
                .map { it as TypeElement }
                .map { it.asType() as DeclaredType }
                .forEach { scopeDeclaredType ->
                    visit(setOf(), scopes, scopeDeclaredType)
                }

        return SourceSet(scopes.values.toList())
    }

    private fun visit(
            visited: Set<Type>,
            scopes: MutableMap<Type, ScopeClass>,
            scopeDeclaredType: DeclaredType) {
        val scopeType = scopeDeclaredType.ir
        if (scopeType in visited) return
        scopes.computeIfAbsent(scopeType) { type ->
            val scopeClass = scopeClassFactory.create(scopeDeclaredType)
            val newVisited = visited + type
            scopeClass.childMethods.forEach {
                val childScopeDeclaredType = it.scope.userData as DeclaredType
                visit(newVisited, scopes, childScopeDeclaredType)
            }
            scopeClass
        }
    }
}