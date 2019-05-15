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
package motif.models.parsing

import motif.ast.IrType
import motif.models.motif.ScopeClass
import motif.models.motif.SourceSet

class SourceSetParser {

    private val scopeClassFactory = ScopeClassParser()

    fun parse(scopeAnnotatedTypes: Set<IrType>): SourceSet {
        val scopeClasses: MutableMap<IrType, ScopeClass> = mutableMapOf()
        scopeAnnotatedTypes.forEach { scopeType ->
            visit(setOf(), scopeClasses, scopeType)
        }
        return SourceSet(scopeClasses.values.toList())
    }

    private fun visit(
            visited: Set<IrType>,
            scopes: MutableMap<IrType, ScopeClass>,
            scopeType: IrType) {
        if (scopeType in visited) return
        scopes.computeIfAbsent(scopeType) { type ->
            val scopeClass = scopeClassFactory.parse(scopeType)
            val newVisited = visited + type
            scopeClass.childMethods.forEach { childMethod ->
                visit(newVisited, scopes, childMethod.scope)
            }
            scopeClass
        }
    }
}
