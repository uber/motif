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

import motif.models.DuplicatedChildParameterSource

internal class DuplicatedChildParameterSourceHandler(
    private val error: DuplicatedChildParameterSource,
) : ErrorHandler {

  override val name = "DUPLICATED CHILD PARAMETER SOURCE"

  override fun StringBuilder.handle() {
    appendLine(
        """
            Multiple child method parameters of the same type:

              ${error.childScopeMethod.qualifiedName}(${highlightDuplicatedParameters()})
      """
            .trimIndent(),
    )
  }

  private fun highlightDuplicatedParameters(): String {
    val names =
        error.childScopeMethod.parameters.map {
          if (it in error.duplicatedParameters) {
            "*${it.type.simpleName}"
          } else {
            it.type.simpleName
          }
        }
    return names.joinToString(", ")
  }
}
