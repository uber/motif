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

import motif.core.UnexposedSourceError

internal class UnexposedSourceHandler(private val error: UnexposedSourceError) : ErrorHandler {

  override val name = "UNEXPOSED SOURCE"

  override fun StringBuilder.handle() {
    appendLine("Dependency source is not annotated with @Expose but is required by a descendant:")
    appendLine()
    appendLine("  [Source]")
    appendLine(error.source.errorText.prependIndent("    "))
    appendLine()
    appendLine("  [Required by]")
    appendLine(error.sink.errorText.prependIndent("    "))
    appendLine()
    appendLine(
        """Suggestions:
            |  * Annotate the source with @Expose.
            |  * Resolve the descendant dependency elsewhere.
        """.trimMargin())
  }
}
