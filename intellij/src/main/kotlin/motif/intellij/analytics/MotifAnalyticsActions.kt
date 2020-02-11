/*
 * Copyright (c) 2018-2019 Uber Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http:www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package motif.intellij.analytics

abstract class MotifAnalyticsActions {

    companion object {
        const val PROJECT_OPENED = "projectOpened"

        const val GRAPH_INIT = "graphInit"
        const val GRAPH_UPDATE = "graphUpdate"
        const val GRAPH_UPDATE_SUCCESS = "graphUpdateSuccess"
        const val GRAPH_UPDATE_ERROR = "graphUpdateError"
        const val GRAPH_COMPUTATION_ERROR = "graphComputationError"

        const val GRAPH_MENU_CLICK = "graphMenuClick"
        const val ANCESTOR_MENU_CLICK = "ancestorMenuClick"
        const val USAGE_MENU_CLICK = "usageMenuClick"
        const val ANCESTOR_GUTTER_CLICK = "ancestorGutterClick"
        const val NAVIGATION_GUTTER_CLICK = "navigationGutterClick"
    }
}