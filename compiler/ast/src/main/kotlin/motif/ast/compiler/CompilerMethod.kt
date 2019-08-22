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

import motif.ast.IrAnnotation
import motif.ast.IrMethod
import motif.ast.IrModifier
import motif.ast.IrType
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ExecutableType

class CompilerMethod(
        override val env: ProcessingEnvironment,
        val owner: DeclaredType,
        val type: ExecutableType,
        val element: ExecutableElement) : IrMethod, IrUtil {

    override val name: String = element.simpleName.toString()

    override val isConstructor: Boolean = element.kind == ElementKind.CONSTRUCTOR

    override val annotations: List<IrAnnotation> by lazy {
        element.irAnnotations()
    }

    override val modifiers: Set<IrModifier> by lazy { element.irModifiers() }

    override val parameters: List<CompilerMethodParameter> by lazy {
        val parameters = element.parameters
        val types = type.parameterTypes
        (0 until parameters.size).map { i ->
            CompilerMethodParameter(env, parameters[i], types[i])
        }
    }

    override val returnType: IrType by lazy {
        CompilerType(env, type.returnType)
    }
}
