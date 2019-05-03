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
package motif.ast

interface IrType : IrEquivalence {

    val qualifiedName: String
    val isVoid: Boolean

    val simpleName: String
        get() = simpleName(qualifiedName)

    fun resolveClass(): IrClass?
    fun isAssignableTo(type: IrType): Boolean
}

private val SEPARATORS = setOf(',', ' ', '<', '>')

internal fun simpleName(qualifiedName: String): String {
    val sb = StringBuilder()
    var skipRest = false
    qualifiedName.reversed().forEach { c ->
        if (SEPARATORS.contains(c)) {
            skipRest = false
        }
        if (c == '.') {
            skipRest = true
        } else if (!skipRest) {
            sb.append(c)
        }
    }
    return sb.reversed().toString()
}
