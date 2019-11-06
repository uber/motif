/*
 * Copyright (c) 2018-2019 Uber Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package motif.models

import motif.ast.IrClass
import motif.ast.IrType

/**
 * [Wiki](https://github.com/uber/motif/wiki#scope)
 */
class Scope internal constructor(val clazz: IrClass) {

    val simpleName: String by lazy { clazz.simpleName }

    val qualifiedName: String by lazy { clazz.qualifiedName }

    val objects: Objects? = Objects.fromScope(this)

    val scopeMethods = clazz.methods.map { method -> ScopeMethod.fromScopeMethod(this, method) }

    val accessMethods: List<AccessMethod> = scopeMethods.mapNotNull { method -> method as? AccessMethod }

    val childMethods: List<ChildMethod> = scopeMethods.mapNotNull { method -> method as? ChildMethod }

    val factoryMethods: List<FactoryMethod> = objects?.factoryMethods ?: emptyList()

    val dependencies: Dependencies? = Dependencies.fromScope(this)

    val source = ScopeSource(this)

    companion object {

        fun fromClasses(scopeClasses: List<IrClass>): List<Scope> {
            return ScopeFactory(scopeClasses).create()
        }
    }
}

private class ScopeFactory(
        private val initialScopeClasses: List<IrClass>) {

    private val scopeMap: MutableMap<IrType, Scope> = mutableMapOf()
    private val visited: MutableSet<IrType> = mutableSetOf()

    fun create(): List<Scope> {
        initialScopeClasses.forEach(this::visit)
        return scopeMap.values.toList()
    }

    private fun visit(scopeClass: IrClass) {
        if (scopeClass.kind != IrClass.Kind.INTERFACE) throw ScopeMustBeAnInterface(scopeClass)

        val scopeType = scopeClass.type

        if (visited.contains(scopeType)) return
        visited.add(scopeType)

        if (!scopeMap.containsKey(scopeType)) {
            val scope = Scope(scopeClass)
            scope.childMethods.forEach { childMethod ->
                visit(childMethod.childScopeClass)
            }
            scopeMap[scopeType] = scope
        }
    }
}
