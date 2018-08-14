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
package motif.ir.graph

import motif.ir.source.ScopeClass
import motif.ir.source.accessmethod.AccessMethod
import motif.ir.source.base.Dependency
import motif.ir.source.child.ChildMethod
import motif.ir.source.dependencies.RequiredDependencies
import motif.ir.source.objects.ObjectsClass

class Scope(
        val scopeClass: ScopeClass,
        val childRequiredDependencies: RequiredDependencies,
        val requiredDependencies: RequiredDependencies) {

    val childMethods: List<ChildMethod> = scopeClass.childMethods
    val accessMethods: List<AccessMethod> = scopeClass.accessMethods
    val objectsClass: ObjectsClass? = scopeClass.objectsClass
    val scopeDependency: Dependency = scopeClass.scopeDependency
}