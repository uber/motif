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

import motif.ast.IrClass

/**
 * [Wiki](https://github.com/uber/motif/wiki#objects)
 */
class Objects private constructor(
        val clazz: IrClass,
        val scope: Scope) {

    val qualifiedName: String by lazy { clazz.qualifiedName }

    val simpleName: String by lazy { clazz.simpleName }

    val factoryMethods = clazz.methods
            .map { method -> FactoryMethod.fromObjectsMethod(this, method) }

    companion object {

        fun fromScope(scope: Scope): Objects? {
            val objectsClass = scope.clazz.annotatedInnerClass(motif.Objects::class) ?: return null

            if (objectsClass.fields.any { !it.isStatic() }) throw ObjectsFieldFound(scope, objectsClass)
            if (objectsClass.hasNonDefaultConstructor()) throw ObjectsConstructorFound(scope, objectsClass)

            return Objects(objectsClass, scope)
        }
    }
}
