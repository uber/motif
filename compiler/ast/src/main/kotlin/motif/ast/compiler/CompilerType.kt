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

import androidx.room.compiler.processing.ExperimentalProcessingApi
import androidx.room.compiler.processing.XProcessingEnv
import androidx.room.compiler.processing.XType
import androidx.room.compiler.processing.isVoid
import com.uber.xprocessing.ext.hash
import com.uber.xprocessing.ext.isDeclaredType
import com.uber.xprocessing.ext.isEquivalent
import com.uber.xprocessing.ext.isInternal
import com.uber.xprocessing.ext.isPrimitive
import com.uber.xprocessing.ext.makeNonNullByDefault
import com.uber.xprocessing.ext.mapToJavaType
import com.uber.xprocessing.ext.mapToKotlinType
import com.uber.xprocessing.ext.qualifiedName
import com.uber.xprocessing.ext.typeUtils
import motif.ast.IrClass
import motif.ast.IrType

@OptIn(ExperimentalProcessingApi::class)
class CompilerType(private val env: XProcessingEnv, mirror: XType) : IrType {

  val mirror = mirror.makeNonNullByDefault()

  fun isInterface(): Boolean = IrClass.Kind.INTERFACE == resolveClass()?.kind

  override val qualifiedName: String by lazy { mirror.qualifiedName(env) }

  override val isVoid: Boolean by lazy { mirror.isVoid() }

  override val isPrimitive: Boolean by lazy { mirror.isPrimitive() }

  override fun resolveClass(): IrClass? =
      if (!mirror.isDeclaredType()) null else CompilerClass(env, mirror)

  override fun isAssignableTo(type: IrType): Boolean {
    val baseMirror = (type as CompilerType).mirror

    if (!env.typeUtils.isAssignable(mirror, baseMirror)) {
      return false
    }

    if (env.typeUtils.isSameType(mirror, baseMirror)) {
      return true
    }

    if (!mirror.isDeclaredType() || !baseMirror.isDeclaredType()) {
      return env.typeUtils.isAssignable(mirror, baseMirror)
    }

    val matchingType = getMatchingSuperType(baseMirror, mirror) ?: return false

    if (baseMirror.typeArguments.isEmpty()) {
      return true
    }

    if (matchingType.typeArguments.size != baseMirror.typeArguments.size) {
      return false
    }

    return if (matchingType.typeArguments.isEmpty() && baseMirror.typeArguments.isEmpty()) {
      env.typeUtils.isAssignable(matchingType, baseMirror)
    } else {
      matchingType.rawType.isAssignableFrom(baseMirror.rawType)
    }
  }

  private fun getMatchingSuperType(baseType: XType, type: XType): XType? {
    val baseErasure = env.typeUtils.erasure(baseType, env)
    val erasure = env.typeUtils.erasure(type, env)
    if (env.typeUtils.isSameType(baseErasure, erasure)) {
      return type
    }

    return env.typeUtils
        .directSupertypes(type)
        .asSequence()
        .mapNotNull { superType -> getMatchingSuperType(baseType, superType) }
        .firstOrNull()
  }

  fun isInternal() = mirror.isInternal()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as CompilerType
    if (!mirror.isEquivalent(other.mirror, env)) return false

    return true
  }

  override fun hashCode(): Int = mirror.hash()

  override fun toString(): String = mirror.toString()

  fun mapToJavaType(): CompilerType = CompilerType(env, mirror.mapToJavaType(env))

  fun mapToKotlinType(): CompilerType = CompilerType(env, mirror.mapToKotlinType(env))
}
