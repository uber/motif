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

import motif.models.NotAssignableBindsMethod

internal class NotAssignableBindsMethodHandler(private val error: NotAssignableBindsMethod) : ErrorHandler {

    override val name = "INVALID BINDS"

    override fun StringBuilder.handle() {
        appendLine("""
            Binds factory method parameter is not assignable to return type:

              [Factory Method]
                ${error.objects.qualifiedName}.${error.method.name}

              [Return Type]
                ${error.returnType.qualifiedName}

              [Parameter Type]
                ${error.parameterType.qualifiedName}
        """.trimIndent())
    }
}
