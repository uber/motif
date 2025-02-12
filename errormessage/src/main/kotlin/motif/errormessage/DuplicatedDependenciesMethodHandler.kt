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

import motif.models.DuplicatedDependenciesMethod

internal class DuplicatedDependenciesMethodHandler(
    private val error: DuplicatedDependenciesMethod,
) : ErrorHandler {

  override val name = "DUPLICATED DEPENDENCIES METHOD"

  override fun StringBuilder.handle() {
    appendLine("Multiple dependencies methods of the same type:\n")
    append(error.duplicatedMethods.joinToString("\n") { "  * ${it.qualifiedName}" })
    appendLine()
    appendLine()
    appendLine(
        """
            Suggestions:
              * Remove the redundant methods
      """
            .trimIndent(),
    )
  }
}
