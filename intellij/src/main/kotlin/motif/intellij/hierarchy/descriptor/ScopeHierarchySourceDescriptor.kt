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
package motif.intellij.hierarchy.descriptor

import com.intellij.icons.AllIcons
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ui.util.CompositeAppearance
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import motif.ast.intellij.IntelliJClass
import motif.ast.intellij.IntelliJMethod
import motif.core.ResolvedGraph
import motif.intellij.ScopeHierarchyUtils.Companion.formatQualifiedName
import motif.models.*
import javax.swing.Icon

open class ScopeHierarchySourceDescriptor(
        project: Project,
        graph: ResolvedGraph,
        parentDescriptor: HierarchyNodeDescriptor?,
        val source: Source)
    : ScopeHierarchyNodeDescriptor(project, graph, parentDescriptor, getElementFromSource(source), false) {

    companion object {
        fun getElementFromSource(source: Source): PsiElement {
            return when (source) {
                is FactoryMethodSource -> {
                    (source.factoryMethod.method as IntelliJMethod).psiMethod
                }
                is ScopeSource -> {
                    (source.scope.clazz as IntelliJClass).psiClass
                }
                is SpreadSource -> {
                    (source.spreadMethod.method as IntelliJMethod).psiMethod
                }
                is ChildParameterSource -> {
                    (source.parameter.method.method as IntelliJMethod).psiMethod
                }
            }
        }
    }

    override fun updateText(text: CompositeAppearance) {
        if (source.isExposed) {
            text.ending.addText("@Expose ")
        }
        text.ending.addText(source.type.simpleName)
        text.ending.addText(" (" + formatQualifiedName(source.type.qualifiedName) + ")", getPackageNameAttributes())
    }

    override fun getLegend(): String? {
        // TODO
        return super.getLegend()
    }

    override fun getIcon(element: PsiElement): Icon? {
        return if (element is PsiClass && element.isInterface) AllIcons.Nodes.Interface else AllIcons.Nodes.Class
    }
}

