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

import motif.Dependencies
import motif.ast.IrClass
import motif.models.errors.DependencyMethodWithParameters
import motif.models.errors.VoidDependenciesMethod
import motif.models.motif.dependencies.Dependency
import motif.models.motif.dependencies.ExplicitDependencies

class ExplicitDependenciesParser : ParserUtil {

    fun parse(scopeClass: IrClass): ExplicitDependencies? {
        val explicitDependenciesClass: IrClass = scopeClass.annotatedInnerClass(Dependencies::class) ?: return null
        val dependencies: List<Dependency> = explicitDependenciesClass.methods
                .onEach { method ->
                    if (method.isVoid()) throw VoidDependenciesMethod(method)
                    if (method.hasParameters()) throw DependencyMethodWithParameters(method)
                }
                .map { it.returnedDependency() }
        return ExplicitDependencies(explicitDependenciesClass.type, dependencies)
    }
}