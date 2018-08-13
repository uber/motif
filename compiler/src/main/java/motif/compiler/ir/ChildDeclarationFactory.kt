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

import com.google.auto.common.MoreTypes
import motif.Scope
import motif.compiler.ir
import motif.compiler.javax.Executable
import motif.compiler.javax.JavaxUtil
import motif.ir.source.child.ChildMethod
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror

class ChildDeclarationFactory(override val env: ProcessingEnvironment) : JavaxUtil {

    fun isApplicable(executable: Executable): Boolean {
        val returnMirror: TypeMirror = executable.returnType
        if (returnMirror.kind != TypeKind.DECLARED) {
            return false
        }

        val returnType: DeclaredType = returnMirror as DeclaredType
        return MoreTypes.asElement(returnType).hasAnnotation(Scope::class)
    }

    fun create(executable: Executable): ChildMethod {
        val childScopeType: DeclaredType = executable.returnType as DeclaredType
        val dynamicDependencies = executable.parameters.map { it.dependency }
        return ChildMethod(executable, childScopeType.ir, dynamicDependencies)
    }
}