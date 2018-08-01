package motif.ir.source.dependencies

import motif.ir.source.base.Dependency
import motif.ir.source.base.Type

class Dependencies(val list: List<AnnotatedDependency>) {

    private val map: Map<Dependency, AnnotatedDependency> by lazy {
        list.associateBy { it.dependency }
    }

    fun toTransitive(): Dependencies {
        return Dependencies(list.map { AnnotatedDependency(it.dependency, true, it.consumingScopes) })
    }

    fun filter(filter: (AnnotatedDependency) -> Boolean): Dependencies {
        return Dependencies(map.values.filter(filter))
    }

    operator fun get(dependency: Dependency): AnnotatedDependency? {
        return map[dependency]
    }

    operator fun minus(dependencyList: List<Dependency>): Dependencies {
        return Dependencies((map - dependencyList).values.toList())
    }

    operator fun plus(dependencies: Dependencies): Dependencies {
        val annotatedDependencies = map.keys.plus(dependencies.map.keys).toSet().map { dependency ->
            val isTransitive = isTransitive(dependency) || dependencies.isTransitive(dependency)
            val consumingScopes = consumingScopes(dependency) + dependencies.consumingScopes(dependency)
            AnnotatedDependency(dependency, isTransitive, consumingScopes)
        }
        return Dependencies(annotatedDependencies)
    }

    private fun isTransitive(dependency: Dependency): Boolean {
        return map[dependency]?.transitive ?: false
    }

    private fun consumingScopes(dependency: Dependency): Set<Type> {
        return map[dependency]?.consumingScopes ?: setOf()
    }

    override fun toString(): String {
        return "${map.values}"
    }
}