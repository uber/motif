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

    abstract val method: IrMethod

    companion object {

        fun fromScopeMethod(scope: Scope, method: IrMethod): ScopeMethod {
            if (method.isVoid()) {
                throw VoidScopeMethod(scope, method)
            }

            val returnClass: IrClass? = method.returnType.resolveClass()

            if (returnClass == null && !method.returnType.isPrimitive) {
                // resolve class does not return IrClass if it is primitive,
                // hence we need to throw only if not primitive and not resolved.
                throw CannotResolveType(scope, method.returnType)
            }

            if (returnClass != null && returnClass.hasAnnotation(motif.Scope::class)) {
                method.parameters.find { it.isNullable() }?.let { nullableParameter ->
                    throw NullableDynamicDependency(scope, method, nullableParameter)
                }
                val childMethod = ChildMethod(method, scope, returnClass)
                val duplicatedParameterTypes = childMethod.parameters - childMethod.parameters.distinctBy { it.type }
                if (duplicatedParameterTypes.isNotEmpty()) {
                    throw DuplicatedChildParameterSource(scope, childMethod, duplicatedParameterTypes)
                }
                return childMethod
            }

            if (method.hasParameters()) {
                throw AccessMethodParameters(scope, method)
            }

            return AccessMethod(method, scope)
        }
    }
}

/**
 * [Wiki](https://github.com/uber/motif/wiki#access-methods)
 */
class AccessMethod(override val method: IrMethod, val scope: Scope) : ScopeMethod() {

    val qualifiedName: String by lazy { "${scope.qualifiedName}.${method.name}" }

    val returnType = Type.fromReturnType(method)

    val sink = AccessMethodSink(this)
}

/**
 * [Wiki](https://github.com/uber/motif/wiki#child-methods)
 */
class ChildMethod(
        override val method: IrMethod,
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
