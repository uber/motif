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
package motif.compiler.ir

import motif.models.java.IrAnnotation
import motif.models.java.IrParameter
import motif.models.java.IrType
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror

class CompilerMethodParameter(
        override val env: ProcessingEnvironment,
        val element: VariableElement,
        val typeMirror: TypeMirror) : IrUtil, IrParameter {


    override val type: IrType by lazy {
        CompilerType(env, typeMirror)
    }

    override val name: String by lazy {
        element.simpleName.toString()
    }

    override val annotations: List<IrAnnotation> by lazy {
        element.irAnnotations()
    }
}