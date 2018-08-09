package motif.compiler.codegen

import motif.compiler.qualifiedName
import motif.ir.graph.Graph
import motif.ir.graph.Scope
import javax.annotation.processing.ProcessingEnvironment

class Generator(
        env: ProcessingEnvironment,
        private val graph: Graph) : CodegenCache(env, CacheScope()) {

    private val scopeImplFactory = ScopeImplFactory(env, cacheScope, graph)

    fun generate() {
        graph.scopes
                .filter {
                    !it.isImplGenerated()
                }
                .forEach {
                    scopeImplFactory.create(it)
                }
    }

    private fun Scope.isImplGenerated(): Boolean {
        val scopeImplName = implTypeName.qualifiedName()
        return env.elementUtils.getTypeElement(scopeImplName) != null
    }
}