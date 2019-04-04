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
package motif.models.motif.objects

import motif.models.java.IrMethod
import motif.models.java.IrType
import motif.models.motif.dependencies.Dependency
import motif.models.motif.dependencies.RequiredDependencies

class FactoryMethod(
        val ir: IrMethod,
        val kind: Kind,
        val scopeType: IrType,
        val isExposed: Boolean,
        val isCached: Boolean,
        val requiredDependencies: RequiredDependencies,
        val providedDependency: Dependency,
        val spreadDependency: SpreadDependency?) {

    val isAbstract: Boolean = ir.isAbstract()
    val isStatic: Boolean = ir.isStatic()

    val providedDependencies: List<Dependency> by lazy {
        (spreadDependency?.methods?.map { it.dependency } ?: listOf()) + providedDependency
    }

    enum class Kind {
        BASIC,
        BINDS,
        CONSTRUCTOR;
    }
}