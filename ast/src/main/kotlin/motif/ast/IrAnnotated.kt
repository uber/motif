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
package motif.ast

import javax.inject.Qualifier
import kotlin.reflect.KClass

interface IrAnnotated {

    val annotations: List<IrAnnotation>

    fun hasAnnotation(annotationClass: KClass<out Annotation>): Boolean {
        return annotations.any { it.matchesClass(annotationClass) }
    }

    fun getQualifier(): IrAnnotation? {
        return annotations.find { annotation ->
            val annotationClass: IrClass = annotation.type.resolveClass() ?: return@find false
            annotationClass.hasAnnotation(Qualifier::class)
        }
    }

    fun isNullable(): Boolean {
        return annotations.any { it.type.simpleName == "Nullable" }
    }
}