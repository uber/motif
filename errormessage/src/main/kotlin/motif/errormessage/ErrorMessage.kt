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

import motif.core.ResolvedGraph
import motif.models.MotifError

class ErrorMessage(val name: String, val text: String) {

    companion object {

        val header = """

            ====================================
                        Motif Errors
            ====================================


            """.trimIndent()

        val footer = """

            ====================================
            """.trimIndent()

        fun toString(graph: ResolvedGraph): String {
            val content: String = graph.errors.joinToString(
                    "\n------------------------------------\n\n") { error ->
                val message = ErrorMessage.get(graph, error)
                "[${message.name}]\n\n${message.text}"
            }
            return "$header$content$footer"
        }

        fun get(graph: ResolvedGraph, error: MotifError): ErrorMessage {
            val handler = ErrorHandler.get(graph, error)
            val sb = StringBuilder()
            handler.apply {
                sb.handle()
            }
            val message = sb.toString()
            return ErrorMessage(handler.name, message)
        }
    }
}
