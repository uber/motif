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
package motif.models

import motif.Expose
import motif.ast.IrClass
import motif.ast.IrMethod
import motif.ast.IrParameter

sealed class ScopeMethod {

    companion object {

        fun fromScopeMethod(scope: Scope, method: IrMethod): ScopeMethod {
            val returnClass: IrClass? = method.returnType.resolveClass()
            if (returnClass != null && returnClass.hasAnnotation(motif.Scope::class)) {
                return ChildMethod(method, scope, returnClass)
            }

            if (!method.hasParameters() && !method.isVoid()) {
                return AccessMethod(method, scope)
            }

            throw InvalidScopeMethod(scope, method)
        }
    }
}

/**
 * [Wiki](https://github.com/uber/motif/wiki#access-methods)
 */
class AccessMethod(val method: IrMethod, val scope: Scope) : ScopeMethod() {

    val qualifiedName: String by lazy { "${scope.qualifiedName}.${method.name}" }

    val returnType = Type.fromReturnType(method)

    val sink = AccessMethodSink(this)
}

/**
 * [Wiki](https://github.com/uber/motif/wiki#child-methods)
 */
class ChildMethod(
        val method: IrMethod,
        val scope: Scope,
        val childScopeClass: IrClass) : ScopeMethod() {

    val qualifiedName: String by lazy { "${scope.qualifiedName}.${method.name}" }

    val parameters: List<Parameter> = method.parameters.map { Parameter(this, it) }

    val sources: List<ChildParameterSource> = parameters.map(::ChildParameterSource)

    class Parameter(val method: ChildMethod, val parameter: IrParameter) {

        val type = Type.fromParameter(parameter)
        val isExposed = parameter.hasAnnotation(Expose::class)
    }
}
