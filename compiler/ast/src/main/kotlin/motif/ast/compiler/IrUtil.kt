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

import androidx.room.compiler.processing.ExperimentalProcessingApi
import androidx.room.compiler.processing.XElement
import androidx.room.compiler.processing.XProcessingEnv
import com.uber.xprocessing.ext.modifierNames
import motif.ast.IrAnnotation
import motif.ast.IrModifier

@OptIn(ExperimentalProcessingApi::class)
interface IrUtil {

  val env: XProcessingEnv

  fun XElement.irModifiers(): Set<IrModifier> = modifierNames.map { IrModifier.valueOf(it) }.toSet()

  fun XElement.irAnnotations(): List<IrAnnotation> =
      getAllAnnotations().map { CompilerAnnotation(env, it) }
}
