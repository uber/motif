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
package motif.models

import motif.ast.IrClass
import motif.ast.IrMethod

class Spread(val clazz: IrClass, val factoryMethod: FactoryMethod) {

    val sourceType: Type by lazy { factoryMethod.returnType.type }
    val qualifiedName: String by lazy { clazz.qualifiedName }

    val methods: List<Method> = clazz.methods
            .filter { method -> isSpreadMethod(method) }
            .onEach { method ->
                if (method.annotations.any { it.type.simpleName == "Nullable" }) {
                    throw NullableSpreadMethod(factoryMethod.objects, factoryMethod.method, clazz, method)
                }
            }
            .map { method -> Method(method, this, Type.fromReturnType(method)) }

    class Method(val method: IrMethod, val spread: Spread, val returnType: Type) {

        val name = method.name
        val sourceType: Type by lazy { spread.sourceType }
        val qualifiedName: String by lazy { "${spread.qualifiedName}.${method.name}" }
    }

    companion object {

        private fun isSpreadMethod(method: IrMethod): Boolean {
            return !method.isVoid() && method.isPublic() && !method.hasParameters()
        }
    }
}