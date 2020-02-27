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
package motif.intellij.analytics

import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import java.util.*

class AnalyticsProjectComponent(val project: Project) : ProjectComponent {

    companion object {
        private val LOGGER_EXTENSION_POINT_NAME : ExtensionPointName<MotifAnalyticsLogger> =
                ExtensionPointName.create("com.uber.motif.motifAnalyticsLogger")
        private val SESSION_ID = UUID.randomUUID()

        fun getInstance(project: Project): AnalyticsProjectComponent {
            return project.getComponent(AnalyticsProjectComponent::class.java)
        }
    }

    fun logEvent(action: String) {
        val metadata: Map<String, String> = mapOf(
                "SessionId" to SESSION_ID.toString(),
                "ActionName" to action)
        LOGGER_EXTENSION_POINT_NAME.getExtensions(project).forEach { it.log(metadata) }
    }
}