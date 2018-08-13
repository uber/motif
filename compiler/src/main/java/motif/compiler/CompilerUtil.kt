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
package motif.compiler

import com.google.auto.common.AnnotationMirrors
import com.squareup.javapoet.ClassName
import motif.compiler.errors.parsing.ParsingError
import motif.ir.source.base.Type
import javax.inject.Qualifier
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.lang.model.type.TypeMirror

val TypeMirror.ir: Type
    get() = Type(this, toString())

val AnnotationMirror.ir: motif.ir.source.base.Annotation
    get() = motif.ir.source.base.Annotation(this, toString())

fun Element.qualifierAnnotation(): motif.ir.source.base.Annotation? {
    val qualifiers = AnnotationMirrors.getAnnotatedAnnotations(this, Qualifier::class.java)
    if (qualifiers.isEmpty()) {
        return null
    }
    if (qualifiers.size > 1) {
        throw ParsingError(this, "More than one qualifier found: $qualifiers.")
    }
    return qualifiers.first().ir
}

fun ClassName.qualifiedName(): String {
    return "${packageName()}.${simpleNames().joinToString(".")}"
}

val GENERATED_DEPENDENCIES_NAME = "Dependencies"