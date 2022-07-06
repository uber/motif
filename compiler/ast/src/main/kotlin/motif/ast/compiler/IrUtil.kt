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
package motif.ast.compiler

import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import motif.ast.IrAnnotation
import motif.ast.IrModifier

interface IrUtil {

  val env: ProcessingEnvironment

  fun Element.irModifiers(): Set<IrModifier> {
    return modifiers.map { IrModifier.valueOf(it.name) }.toSet()
  }

  fun Element.irAnnotations(): List<IrAnnotation> {
    return annotationMirrors.map { CompilerAnnotation(env, it) }
  }
}
