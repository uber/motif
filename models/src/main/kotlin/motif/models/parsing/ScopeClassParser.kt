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
package motif.models.parsing

import motif.models.java.IrClass
import motif.models.java.IrType
import motif.models.parsing.errors.InvalidScopeMethod
import motif.models.parsing.errors.ScopeMustBeAnInterface
import motif.models.motif.ScopeClass
import motif.models.motif.accessmethod.AccessMethod
import motif.models.motif.child.ChildMethod
import motif.models.motif.dependencies.ExplicitDependencies
import motif.models.motif.objects.ObjectsClass

class ScopeClassParser {

    private val childMethodParser = ChildMethodParser()
    private val accessMethodParser = AccessMethodParser()
    private val objectsClassParser = ObjectsClassParser()
    private val explicitDependenciesParser = ExplicitDependenciesParser()

    fun parse(scopeType: IrType): ScopeClass {
        val irScopeClass = scopeType.resolveClass() ?: throw IllegalStateException()
        if (irScopeClass.kind != IrClass.Kind.INTERFACE) throw ScopeMustBeAnInterface(irScopeClass)

        val childMethods: MutableList<ChildMethod> = mutableListOf()
        val accessMethods: MutableList<AccessMethod> = mutableListOf()

        irScopeClass.methods
                .forEach {
                    when {
                        childMethodParser.isApplicable(it) -> childMethods.add(childMethodParser.parse(it))
                        accessMethodParser.isApplicable(it) -> accessMethods.add(accessMethodParser.parse(it))
                        else -> throw InvalidScopeMethod(it)
                    }
                }

        val objectsClass: ObjectsClass? = objectsClassParser.parse(irScopeClass)
        val explicitDependencies: ExplicitDependencies? = explicitDependenciesParser.parse(irScopeClass)

        return ScopeClass(
                irScopeClass,
                childMethods,
                accessMethods,
                objectsClass,
                explicitDependencies)
    }
}