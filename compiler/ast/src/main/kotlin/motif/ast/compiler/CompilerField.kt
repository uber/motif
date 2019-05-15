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

import motif.ast.IrField
import motif.ast.IrModifier
import motif.ast.IrType
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.VariableElement

class CompilerField(
        override val env: ProcessingEnvironment,
        private val variableElement: VariableElement) : IrUtil, IrField {

    override val type: IrType by lazy {
        CompilerType(env, variableElement.asType())
    }

    override val name: String by lazy {
        variableElement.simpleName.toString()
    }

    override val modifiers: Set<IrModifier> by lazy {
        variableElement.irModifiers()
    }
}
