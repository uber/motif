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
package motif.ast.compiler

import com.google.auto.common.AnnotationMirrors
import com.google.common.base.Equivalence
import motif.ast.IrAnnotation
import motif.ast.IrMethod
import motif.ast.IrType
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.TypeElement
import javax.lang.model.type.ExecutableType
import kotlin.reflect.KClass

class CompilerAnnotation(
        env: ProcessingEnvironment,
        val mirror: AnnotationMirror) : IrAnnotation {

    private val key: Equivalence.Wrapper<AnnotationMirror> = AnnotationMirrors.equivalence().wrap(mirror)
    private val className: String by lazy {
        val typeElement = mirror.annotationType.asElement() as TypeElement
        typeElement.qualifiedName.toString()
    }
    private val pretty: String by lazy { mirror.toString() }

    override val type: IrType = CompilerType(env, mirror.annotationType)

    override val members: List<IrMethod> by lazy {
        mirror.elementValues.keys.map {
            val executableType = env.typeUtils.asMemberOf(mirror.annotationType, it) as ExecutableType
            CompilerMethod(env, mirror.annotationType, executableType, it)
        }
    }

    override fun matchesClass(annotationClass: KClass<out Annotation>): Boolean {
        return annotationClass.java.name == className
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CompilerAnnotation

        if (key != other.key) return false

        return true
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }

    override fun toString(): String {
        return pretty
    }
}
