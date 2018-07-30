package motif.compiler.codegen

import com.squareup.javapoet.MethodSpec
import motif.compiler.model.Dependency
import motif.compiler.names.UniqueNameSet
import javax.lang.model.element.Modifier

class ProvisionMethodsSpec(dependencies: Iterable<Dependency>, edit: MethodSpec.Builder.(Dependency) -> Unit = {}) {

    private val names = UniqueNameSet()
    private val specMap: Map<Dependency, MethodSpec> = dependencies.associateBy({ it }) { dependency ->
        MethodSpec.methodBuilder(names.unique(dependency.preferredName)).apply {
            returns(dependency.className)
            addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            edit(dependency)
        }.build()
    }

    val specs: List<MethodSpec> = specMap.toSortedMap().map { it.value }

    operator fun get(dependency: Dependency): MethodSpec? {
        return specMap[dependency]
    }
}