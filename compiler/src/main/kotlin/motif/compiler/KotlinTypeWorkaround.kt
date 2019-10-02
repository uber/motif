/*
 * Copyright (c) 2018-2019 Uber Technologies, Inc.
 * Copyright (C) 2015 Square, Inc.
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

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ExecutableType
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Types
import kotlin.reflect.jvm.internal.impl.builtins.jvm.JavaToKotlinClassMap
import kotlin.reflect.jvm.internal.impl.name.FqName

/**
 * Workarounds for:
 * https://github.com/square/kotlinpoet/issues/236
 */
object KotlinTypeWorkaround {

    fun javaToKotlinType(mirror: TypeMirror): TypeName {
        return javaToKotlinType(asTypeName(mirror))
    }

    fun javaToKotlinType(className: com.squareup.kotlinpoet.ClassName): TypeName {
        return javaToKotlinType(className as TypeName)
    }

    private fun asTypeName(mirror: TypeMirror): TypeName {
        val typeName = mirror.asTypeName()
        val className = typeName as? com.squareup.kotlinpoet.ClassName ?: return typeName
        val declaredType = mirror as? DeclaredType ?: return typeName
        val element = declaredType.asElement() as? TypeElement ?: return typeName

        if (element.typeParameters.isEmpty()) {
            return typeName
        }

        val typeArguments = element.typeParameters.map { STAR }
        return className.parameterizedBy(*typeArguments.toTypedArray())
    }

    /**
     * https://github.com/square/kotlinpoet/issues/236#issuecomment-437961476
     */
    private fun javaToKotlinType(typeName: TypeName): TypeName {
        return when (typeName) {
            is ParameterizedTypeName -> (javaToKotlinType(typeName.rawType) as com.squareup.kotlinpoet.ClassName)
                    .parameterizedBy(
                            *typeName.typeArguments.map { javaToKotlinType(it) }.toTypedArray()
                    )
            is WildcardTypeName -> when {
                typeName.inTypes.isNotEmpty() -> WildcardTypeName.consumerOf(javaToKotlinType(typeName.inTypes.single()))
                typeName.outTypes.isNotEmpty() -> WildcardTypeName.producerOf(javaToKotlinType(typeName.outTypes.single()))
                else -> throw IllegalStateException()
            }
            else -> {
                val className = JavaToKotlinClassMap.INSTANCE
                        .mapJavaToKotlin(FqName(typeName.toString()))?.asSingleFqName()?.asString()
                if (className == null) typeName
                else com.squareup.kotlinpoet.ClassName.bestGuess(className)
            }
        }
    }

    /**
     * Copied from [FunSpec.overriding] and modified to leverage [javaToKotlinType].
     */
    fun overriding(
            method: ExecutableElement,
            enclosing: DeclaredType,
            types: Types
    ): FunSpec.Builder {
        val executableType = types.asMemberOf(enclosing, method) as ExecutableType
        val resolvedParameterTypes = executableType.parameterTypes
        val resolvedReturnType = executableType.returnType

        val builder = FunSpec.overriding(method)
        builder.returns(javaToKotlinType(resolvedReturnType))
        var i = 0
        val size = builder.parameters.size
        while (i < size) {
            val parameter = builder.parameters[i]
            val type = javaToKotlinType(resolvedParameterTypes[i])
            builder.parameters[i] = parameter.toBuilder(parameter.name, type).build()
            i++
        }

        return builder
    }
}