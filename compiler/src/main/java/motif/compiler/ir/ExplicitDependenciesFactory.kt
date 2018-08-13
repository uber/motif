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
package motif.compiler.ir

import motif.Dependencies
import motif.compiler.errors.parsing.ParsingError
import motif.compiler.javax.JavaxUtil
import motif.ir.source.dependencies.ExplicitDependencies
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.type.DeclaredType

class ExplicitDependenciesFactory(override val env: ProcessingEnvironment) : JavaxUtil {

    fun create(scopeType: DeclaredType): ExplicitDependencies? {
        val explicitDependenciesType = scopeType.annotatedInnerType(Dependencies::class) ?: return null
        val depedencies = explicitDependenciesType.methods()
                .onEach {
                    if (it.isVoid) throw ParsingError(it.element, "Dependencies methods must not return void.")
                    if (it.parameters.isNotEmpty()) throw ParsingError(it.element, "Dependencies methods must be parameterless.")
                }
                .map { it.returnedDependency }
        return ExplicitDependencies(explicitDependenciesType, depedencies)
    }
}