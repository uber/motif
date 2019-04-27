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
package motif.models.parsing

import motif.Expose
import motif.Scope
import motif.ast.IrClass
import motif.ast.IrMethod
import motif.models.motif.child.ChildMethod
import motif.models.motif.dependencies.DynamicDependency

class ChildMethodParser : ParserUtil {

    fun isApplicable(method: IrMethod): Boolean {
        val returnClass: IrClass = method.returnType.resolveClass() ?: return false
        return returnClass.hasAnnotation(Scope::class)
    }

    fun parse(method: IrMethod): ChildMethod {
        val dynamicDependencies: List<DynamicDependency> = method.parameters.map {
            DynamicDependency(it.toDependency(), it.hasAnnotation(Expose::class))
        }
        return ChildMethod(method, method.returnType, dynamicDependencies)
    }
}