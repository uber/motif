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
import com.intellij.psi.PsiParameter
import com.intellij.psi.PsiSubstitutor
import motif.models.java.IrAnnotation
import motif.models.java.IrParameter
import motif.models.java.IrType

class IntelliJMethodParameter(
        private val project: Project,
        val psiParameter: PsiParameter,
        val substitutor: PsiSubstitutor) : IrUtil, IrParameter {

    override val type: IrType by lazy {
        IntelliJType(project, substitutor.substitute(psiParameter.type))
    }

    override val name: String by lazy { psiParameter.name!! }

    override val annotations: List<IrAnnotation> by lazy {
        psiParameter.modifierList!!.irAnnotations(project)
    }
}