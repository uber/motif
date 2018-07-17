package com.uber.motif.compiler.codegen

import com.squareup.javapoet.ParameterSpec
import com.uber.motif.compiler.graph.ResolvedChild
import com.uber.motif.compiler.model.Dependency
import com.uber.motif.compiler.names.UniqueNameSet
import java.util.*
import javax.lang.model.element.Modifier

class BridgeMethodParametersSpec(child: ResolvedChild, parent: BridgeParentSpec) {

    private val names = UniqueNameSet()
    private val specMap: SortedMap<Dependency, ParameterSpec> = child.method.dynamicDependencies.associateBy({ it }) {
        ParameterSpec.builder(it.className, names.unique(it.preferredName))
                .addModifiers(Modifier.FINAL)
                .build()
    }.toSortedMap()

    val parentParameter: ParameterSpec = ParameterSpec.builder(parent.className, "parent")
            .addModifiers(Modifier.FINAL)
            .build()
    val dependencies: List<Dependency> = specMap.map { it.key }
    val specs: List<ParameterSpec> = listOf(parentParameter) + specMap.map { it.value }

    operator fun get(dependency: Dependency): ParameterSpec? {
        return specMap[dependency]
    }
}