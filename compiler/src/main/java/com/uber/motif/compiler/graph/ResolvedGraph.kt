package com.uber.motif.compiler.graph

import com.uber.motif.compiler.TypeId
import com.uber.motif.compiler.errors.MissingDependencies
import com.uber.motif.compiler.errors.ScopeImplementationNotFound
import com.uber.motif.compiler.id
import com.uber.motif.compiler.model.Dependency
import com.uber.motif.compiler.model.ParentInterfaceMethod
import com.uber.motif.compiler.model.ScopeClass
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.type.DeclaredType

class ResolvedGraph(val resolvedScopes: Set<ResolvedScope>) {

    private val pretty: String by lazy {
        val scopes = resolvedScopes.map { it.scope.type }.toString()
        "ResolvedGraph$scopes"
    }

    override fun toString(): String {
        return pretty
    }

    companion object {

        fun resolve(env: ProcessingEnvironment, scopeTypes: List<DeclaredType>): ResolvedGraph {
            return GraphResolver(env, scopeTypes).resolve()
        }
    }
}

private class GraphResolver(private val env: ProcessingEnvironment, scopeTypes: List<DeclaredType>) {

    private val scopes: Map<TypeId, ScopeClass> = scopeTypes
            .map { ScopeClass.fromClass(env, it) }
            .associateBy { it.type.id }

    private val resolvedParents: MutableMap<TypeId, ResolvedParent> = mutableMapOf()

    fun resolve(): ResolvedGraph {
        val validatedScopes = scopes.map { (_, scope) ->
            val parent: ResolvedParent = resolveParent(scope.type)
            val children: List<ResolvedChild> = scope.childMethods.map { childMethod ->
                val childParent: ResolvedParent = resolveParent(childMethod.scopeType)
                ResolvedChild(childMethod, childParent)
            }
            ResolvedScope(scope, parent, children)
        }.toSet()
        return ResolvedGraph(validatedScopes)
    }

    private fun resolveParent(scopeType: DeclaredType): ResolvedParent {
        return resolveParent(setOf(), scopeType) ?: throw IllegalStateException()
    }

    // TODO Currently, dynamic dependencies are public by default. This is due to the fact that a child doesn't know
    // whether its external dependencies are provided "normally" or via a dynamic dependency. This is convenient in
    // order to support multiple parents each passing different dynamic dependencies to the child. Ideally, dynamic
    // dependencies would only be public if listed explicitly on the Objects class as public.
    private fun resolveParent(visited: Set<TypeId>, scopeType: DeclaredType): ResolvedParent? {
        // Circular scope dependencies are allowed. Stop recursion when we hit a cycle.
        if (scopeType.id in visited) return null
        return resolvedParents.computeIfAbsent(scopeType.id) {
            // If scope implementation is already generated, don't recalculate dependencies. This is the case for child
            // scopes that live in a different Gradle module.
            ResolvedParent.fromGenerated(env, scopeType)?.let { return@computeIfAbsent it }

            val scope = scopes[scopeType.id]
            // Child scope isn't being processed by the annotation processor and its implementation hasn't been
            // generated yet. This can happen if a child Gradle module didn't run this annotation processor.
                    ?: throw ScopeImplementationNotFound(scopeType)

            val newVisited = visited + scopeType.id

            // Collect child dependencies.
            val childDependencies: Set<Dependency> = scope.childMethods.flatMap {
                val parentInterfaceMethods: List<ParentInterfaceMethod> = resolveParent(newVisited, it.scopeType)?.methods ?: listOf()
                val dependencies: List<Dependency> = parentInterfaceMethods.map { it.dependency }
                val transitiveDependencies: List<Dependency> = parentInterfaceMethods.filter { it.isTransitive }.map { it.dependency }
                val nonTransitiveDependencies: List<Dependency> = dependencies - transitiveDependencies
                // To make dynamic dependencies internal by default, we only allow dynamic dependencies to cover
                // non-transitive dependencies here.
                nonTransitiveDependencies - it.dynamicDependencies + transitiveDependencies
            }.toSet()

            val selfDependencies = scope.dependenciesRequiredBySelf - scope.providedDependencies
            // We can only pass public dependencies to children so use scope.providedPublicDependencies here.
            val transitiveDependencies = childDependencies - scope.providedPublicDependencies

            // We want to prioritize the Dependency.metaDesc stored at highest level so the following is not
            // equivalent to (selfDependencies + transitiveDependencies):
            val externalDependencies = selfDependencies + (transitiveDependencies - selfDependencies)

            // If developer defined a parent interface explicitly, ensure that the dependencies declared on the parent
            // interface cover what this scope requires from its parent. If this scope requires more from its parent
            // than what's defined on the parent interface, throw a missing dependencies error.
            scope.parentInterface?.let { explicitParentInterface ->
                val expectedDependenciesProvidedByParent = explicitParentInterface.dependencies

                val missingDependencies = externalDependencies - expectedDependenciesProvidedByParent
                if (missingDependencies.isNotEmpty()) {
                    throw MissingDependencies(scopeType, missingDependencies)
                }
                // Make sure to use the explicitly declared external dependencies instead of calculated ones.
                return@computeIfAbsent ResolvedParent.fromExplicit(explicitParentInterface)
            }

            ResolvedParent.fromCalculated(scopeType, externalDependencies, transitiveDependencies)
        }
    }
}