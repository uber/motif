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

import motif.models.AccessMethodParameters

internal class AccessMethodParametersHandler(private val error: AccessMethodParameters) :
    ErrorHandler {

  override val name = "ACCESS METHOD PARAMETERS"

  override fun StringBuilder.handle() {
    appendLine(
        """
            Access methods must be parameterless:

              ${error.scope.qualifiedName}.${error.method.name}

            Suggestions:
              * If this method was intended to be a child method, ensure that the return type is a Scope.
      """.trimIndent())
  }
}
