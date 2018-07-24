package com.uber.motif.compiler.codegen

import com.squareup.javapoet.ParameterSpec
import com.uber.motif.compiler.graph.ResolvedChild
import com.uber.motif.compiler.model.BasicChildMethodParameter
import com.uber.motif.compiler.model.Dependency
import com.uber.motif.compiler.model.SpreadChildParameter
import com.uber.motif.compiler.names.UniqueNameSet
import java.util.*
import javax.lang.model.element.Modifier

class BridgeMethodParametersSpec(child: ResolvedChild, parent: BridgeParentSpec) {

    private val names = UniqueNameSet()
    val specs: MutableList<ParameterSpec> = mutableListOf()

    private val specMap: SortedMap<Dependency, Param> = child.method.parameters.flatMap { parameter ->
        val name = parameter.element.simpleName.toString().apply { names.claim(this) }
        val spec = ParameterSpec.builder(parameter.type.typeName, name)
                .addModifiers(Modifier.FINAL)
                .build().apply { specs.add(this) }
        when (parameter) {
            is BasicChildMethodParameter -> listOf(Param(parameter.dependency, spec, null))
            is SpreadChildParameter -> {
                parameter.methods.map { (dependency, method) ->
                    Param(dependency, spec, method.simpleName.toString())
                }
            }
            else -> throw IllegalStateException()
        }
    }.associateBy { it.dependency }.toSortedMap()

    val parentParameter: ParameterSpec = ParameterSpec.builder(parent.className, names.unique("parent"))
            .addModifiers(Modifier.FINAL)
            .build().apply { specs.add(0, this) }

    val dependencies: List<Dependency> = specMap.map { it.key }

    operator fun get(dependency: Dependency): Param? {
        return specMap[dependency]
    }

    class Param(val dependency: Dependency, val spec: ParameterSpec, val spreadMethodName: String?)
}