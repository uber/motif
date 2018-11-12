/*
 * Copyright (c) 2018 Uber Technologies, Inc.
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
package motif.models.motif

import motif.models.java.IrClass
import motif.models.motif.accessmethod.AccessMethod
import motif.models.motif.dependencies.Dependency
import motif.models.motif.child.ChildMethod
import motif.models.motif.dependencies.ExplicitDependencies
import motif.models.motif.dependencies.RequiredDependencies
import motif.models.motif.dependencies.RequiredDependency
import motif.models.motif.objects.FactoryMethod
import motif.models.motif.objects.ObjectsClass

class ScopeClass(
        val ir: IrClass,
        val childMethods: List<ChildMethod>,
        val accessMethods: List<AccessMethod>,
        val objectsClass: ObjectsClass?,
        val explicitDependencies: ExplicitDependencies?) {

    val scopeDependency = Dependency(ir.type, null)

    val factoryMethods: List<FactoryMethod> = objectsClass?.factoryMethods ?: listOf()

    val provided: List<Dependency> = factoryMethods.flatMap { it.providedDependencies } + scopeDependency

    private val consumed: List<Dependency> = factoryMethods.flatMap { it.requiredDependencies.list }.map { it.dependency } + accessMethods.map { it.dependency }

    val notExposed: Map<Dependency, FactoryMethod> = factoryMethods.filter { !it.isExposed }.associateBy { it.providedDependency }

    val selfRequiredDependencies: RequiredDependencies by lazy {
        val requiredDependencies = (consumed - provided).map { RequiredDependency(it, false, setOf(ir.type)) }
        RequiredDependencies(requiredDependencies)
    }

    override fun toString(): String {
        return ir.toString()
    }
}