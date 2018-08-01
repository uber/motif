package motif.ir.source.objects

import motif.ir.source.base.Dependency

class FactoryMethod(
        val userData: Any?,
        val kind: Kind,
        val isExposed: Boolean,
        val isCached: Boolean,
        val consumedDependencies: List<Dependency>,
        val providedDependency: Dependency,
        val spreadDependency: SpreadDependency?) {

    val isAbstract: Boolean = kind.isAbstract

    val providedDependencies: List<Dependency> by lazy {
        (spreadDependency?.methods?.map { it.dependency } ?: listOf()) + providedDependency
    }

    val exposedDependencies: List<Dependency> by lazy {
        if (isExposed) {
            providedDependencies
        } else {
            listOf()
        }
    }

    enum class Kind (val isAbstract: Boolean) {
        BASIC(isAbstract = false),
        BINDS(isAbstract = true),
        CONSTRUCTOR(isAbstract = true);
    }
}