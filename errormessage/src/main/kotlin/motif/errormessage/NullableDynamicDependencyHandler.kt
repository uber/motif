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

import motif.models.NullableDynamicDependency

internal class NullableDynamicDependencyHandler(private val error: NullableDynamicDependency) :
    ErrorHandler {

  override val name = "NULLABLE CHILD METHOD PARAMETER"

  override fun StringBuilder.handle() {
    appendLine(
        """
            Parameter may not be nullable:

              [Method]
                ${error.scope.qualifiedName}.${error.method.name}

              [Parameter]
                @Nullable ${error.parameter.type.qualifiedName} ${error.parameter.name}

            Suggestions:
              * Consider using Optional<...> instead.
      """.trimIndent())
  }
}
