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
package motif.ast

import kotlin.reflect.KClass

interface IrClass : IrAnnotated, IrHasModifiers {

    val type: IrType
    val supertypes: List<IrType>
    val typeArguments: List<IrType>
    val kind: Kind
    val methods: List<IrMethod>
    val nestedClasses: List<IrClass>
    val fields: List<IrField>
    val constructors: List<IrMethod>

    val qualifiedName: String
        get() = type.qualifiedName

    val simpleName: String
        get() = type.simpleName

    fun hasNonDefaultConstructor(): Boolean {
        return constructors.any { it.hasParameters() }
    }

    fun annotatedInnerClass(annotationClass: KClass<out Annotation>): IrClass? {
        return nestedClasses.find { it.hasAnnotation(annotationClass) }
    }

    enum class Kind {
        CLASS, INTERFACE
    }
}
