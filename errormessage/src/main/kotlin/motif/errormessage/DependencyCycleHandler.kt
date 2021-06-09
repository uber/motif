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

import motif.core.DependencyCycleError

internal class DependencyCycleHandler(private val error: DependencyCycleError) : ErrorHandler {

    override val name = "DEPENDENCY CYCLE"

    override fun StringBuilder.handle() {
        appendLine("Dependency cycle detected:")
        appendLine()
        error.path.forEachIndexed { i, node ->
            val prefix = if (i == 0) "  " else "  -> "
            node.errorText.lineSequence().forEachIndexed { index, line ->
                val indent = " ".repeat(prefix.length)
                if (index == 0) {
                    appendLine("$prefix$line")
                } else {
                    appendLine(line.prependIndent(indent))
                }
            }
            appendLine()
        }
    }
}
