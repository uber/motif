/*
 * Copyright (c) 2018 Uber Technologies, Inc.
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
package motif.intellij

import com.intellij.ide.hierarchy.LanguageTypeHierarchy
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import motif.intellij.hierarchy.graph.GraphProcessor
import motif.intellij.hierarchy.ui.MotifScopeHierarchyProvider

class MotifComponent(project: Project) : ProjectComponent {

    val graphProcessor = GraphProcessor(project)

    init {
        // adding the language extension here allows us to be called before Java's type hierarchy extension is
        // called, allowing Motif to create its own tree structure for the hierarchy viewer.
        LanguageTypeHierarchy.INSTANCE.addExplicitExtension(JavaLanguage.INSTANCE, MotifScopeHierarchyProvider.INSTANCE)
    }

    companion object {

        fun get(project: Project): MotifComponent {
            return project.getComponent(MotifComponent::class.java)
        }
    }
}