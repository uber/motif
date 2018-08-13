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

import motif.compiler.javax.Executable
import motif.compiler.javax.JavaxUtil
import motif.ir.source.accessmethod.AccessMethod
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.type.TypeKind

class AccessMethodFactory(override val env: ProcessingEnvironment) : JavaxUtil {

    fun isApplicable(executable: Executable): Boolean {
        return executable.parameters.isEmpty() && executable.returnType.kind != TypeKind.VOID
    }

    fun create(executable: Executable): AccessMethod {
        return AccessMethod(executable, executable.returnedDependency)
    }
}