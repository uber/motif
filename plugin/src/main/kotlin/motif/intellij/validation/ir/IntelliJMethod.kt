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
package motif.intellij.validation.ir

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiMethod
import motif.models.java.*

class IntelliJMethod(
        private val project: Project,
        private val psiMethod: PsiMethod) : IrUtil, IrMethod {

    override val parameters: List<IrParameter> by lazy {
        psiMethod.parameterList.parameters.map { IntelliJMethodParameter(project, it) }
    }

    override val returnType: IrType by lazy { IntelliJType(project, psiMethod.returnType!!) }

    override val name: String by lazy { psiMethod.name }

    override val annotations: List<IrAnnotation> by lazy { psiMethod.modifierList.irAnnotations(project) }

    override val modifiers: Set<IrModifier> by lazy { psiMethod.irModifiers() }
}