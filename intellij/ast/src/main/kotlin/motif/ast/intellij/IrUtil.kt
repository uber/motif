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
import com.intellij.psi.PsiAnnotationOwner
import com.intellij.psi.PsiModifier
import com.intellij.psi.PsiModifierListOwner
import kotlin.collections.filter
import kotlin.collections.map
import kotlin.collections.toSet
import motif.ast.IrAnnotation
import motif.ast.IrModifier

interface IrUtil {

  fun PsiModifierListOwner.irModifiers(): Set<IrModifier> =
      PsiModifier.MODIFIERS.filter { hasModifierProperty(it) }
          .map { IrModifier.valueOf(it.uppercase()) }
          .toSet()

  fun PsiAnnotationOwner.irAnnotations(project: Project): List<IrAnnotation> =
      annotations.map { IntelliJAnnotation(project, it) }
}
