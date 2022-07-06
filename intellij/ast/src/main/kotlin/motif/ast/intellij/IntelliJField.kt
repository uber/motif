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
package motif.ast.intellij

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiField
import motif.ast.IrField
import motif.ast.IrModifier
import motif.ast.IrType

class IntelliJField(private val project: Project, private val psiField: PsiField) :
    IrUtil, IrField {

  override val type: IrType by lazy { IntelliJType(project, psiField.type) }

  override val name: String by lazy { psiField.name }

  override val modifiers: Set<IrModifier> by lazy { psiField.irModifiers() }
}
