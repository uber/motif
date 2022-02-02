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
package motif.errormessage

import motif.models.CannotResolveType

internal class CannotResolveTypeHandler(private val error: CannotResolveType) : ErrorHandler {

    override val name = "CANNOT RESOLVE TYPE"

    override fun StringBuilder.handle() {
        appendLine(
            """
            Following type cannot be resolved in ${error.scope.qualifiedName}:

              ${error.type.qualifiedName}
              
            Suggestions:
              * Check if the module of ${error.type.qualifiedName} is provided as a dependency 
              to the module where the parent scope of ${error.scope.qualifiedName} is defined.
        """.trimIndent()
        )
    }

}