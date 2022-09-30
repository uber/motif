/*
 * Copyright (c) 2022 Uber Technologies, Inc.
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
package motif.compiler

import java.io.File

data class TestParameters(
    @get:JvmName("testDirectory") val testDirectory: File,
    @get:JvmName("processorType") val processorType: ProcessorType,
    @get:JvmName("sourceLanguage") val sourceLanguage: SourceLanguage,
)

enum class ProcessorType {
    AP,
    KSP
}

enum class SourceLanguage {
    JAVA,
    KOTLIN
}
