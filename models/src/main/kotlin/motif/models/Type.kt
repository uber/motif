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
package motif.models

import motif.ast.IrAnnotation
import motif.ast.IrMethod
import motif.ast.IrParameter
import motif.ast.IrType

data class Type(val type: IrType, val qualifier: IrAnnotation?) : Comparable<Type> {

    val qualifiedName: String by lazy {
        val qualifierString = qualifier?.let { "$it " } ?: ""
        "$qualifierString${type.qualifiedName}"
    }

    override fun compareTo(other: Type): Int {
        return compareKey.compareTo(other.compareKey)
    }

    private val compareKey: String by lazy {
        val qualifierString = qualifier?.let { "$it " } ?: ""
        "${type.qualifiedName}$qualifierString"
    }

    companion object {

        fun fromParameter(parameter: IrParameter): Type {
            return Type(parameter.type, parameter.getQualifier())
        }

        fun fromReturnType(method: IrMethod): Type {
            return Type(method.returnType, method.getQualifier())
        }
    }
}