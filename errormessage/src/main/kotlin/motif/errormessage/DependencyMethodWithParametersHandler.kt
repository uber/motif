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

import motif.models.DependencyMethodWithParameters

internal class DependencyMethodWithParametersHandler(
    private val error: DependencyMethodWithParameters,
) : ErrorHandler {

  override val name = "DEPENDENCY METHOD PARAMETER"

  override fun StringBuilder.handle() {
    appendLine(
        """
            Methods on dependencies interfaces must be parameterless:

              ${error.dependenciesClass.qualifiedName}.${error.method.name}
      """
            .trimIndent(),
    )
  }
}
