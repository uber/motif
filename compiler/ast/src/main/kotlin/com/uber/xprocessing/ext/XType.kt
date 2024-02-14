/*
 * Copyright (c) 2022 Uber Technologies, Inc.
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
@file:OptIn(ExperimentalProcessingApi::class)

package com.uber.xprocessing.ext

import androidx.room.compiler.processing.ExperimentalProcessingApi
import androidx.room.compiler.processing.XNullability
import androidx.room.compiler.processing.XProcessingEnv
import androidx.room.compiler.processing.XType
import androidx.room.compiler.processing.compat.XConverters.toJavac
import androidx.room.compiler.processing.compat.XConverters.toKS
import androidx.room.compiler.processing.compat.XConverters.toXProcessing
import androidx.room.compiler.processing.isEnum
import com.google.auto.common.MoreTypes
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getJavaClassByName
import com.google.devtools.ksp.isInternal
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSType
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.WildcardTypeName
import javax.lang.model.type.DeclaredType

/**
 * Since Motif builds the entire ScopeGraph for every module, there are often cases where it is
 * operating on types that are not on the classpath (esp due to ABIs). Because of this, we can only
 * rely on the information provided to us by the toString() of the XType. This adds special handling
 * of the qualified name for various cases to ensure that similar types match.
 */
fun XType.qualifiedName(env: XProcessingEnv): String {
  val mirror = this
  val candidate =
      if (mirror.toString().contains("$") && mirror.isDeclaredType()) {
        (mirror as? DeclaredType)?.asElement()?.kind
        mirror.toString()
      } else {
        when (env.backend) {
          XProcessingEnv.Backend.JAVAC -> mirror.toString()
          XProcessingEnv.Backend.KSP -> {
            if ("NonExistentClass" in mirror.typeName.toString()) {
              // Type contains class not on classpath, so we can't make use of TypeName
              val decl = mirror.toKS().declaration
              val packageName = decl.packageName.asString()
              val simpleName = decl.simpleName.asString()
              val args =
                  if ('<' in mirror.toString()) {
                        mirror.toString().substring(mirror.toString().indexOf('<'))
                      } else {
                        ""
                      }
                      .replace("\\.\\.[A-Za-z0-9._-]+\\?".toRegex(), "")
                      .replace("(", "")
                      .replace(")", "")
              "$packageName.$simpleName$args".replace("? extends ", "").replace("? super ", "")
            } else if (mirror.typeName.containsWildcardType()) {
              mirror.typeName.removeWildcardType().toString()
            } else {
              mirror.typeName.toString()
            }
          }
        }
      }

  return if (candidate.startsWith('(')) {
    candidate.substringAfter(":: ").dropLast(1)
  } else if (candidate.startsWith("@")) {
    candidate.substringAfterLast(" ")
  } else {
    candidate
  }
}

/** Recursively checks the parts of the TypeName checking for existence of a wildcard type. */
fun TypeName.containsWildcardType(): Boolean {
  return when (this) {
    is WildcardTypeName -> true
    is ParameterizedTypeName -> return this.typeArguments.any { it.containsWildcardType() }
    else -> false
  }
}

/** Returns a TypeName with any Wildcard types recursively removed. */
fun TypeName.removeWildcardType(): TypeName {
  return when (this) {
    is WildcardTypeName -> this.upperBounds.first() ?: TypeName.OBJECT
    is ParameterizedTypeName -> {
      val args = this.typeArguments.map { it.removeWildcardType() }.toTypedArray()
      ParameterizedTypeName.get(this.rawType, *args)
    }
    else -> this
  }
}

fun TypeName.removeWildcardTypeIfContains(): TypeName {
  return if (this.containsWildcardType()) this.removeWildcardType() else this
}

/**
 * This adds extra handling to Java TypeNames so that when it is based on an XType that has type
 * parameters, we add the necessary wildcards so that we use a non-raw type so we are compatible
 * with Kotlin types.
 */
fun com.squareup.javapoet.TypeName.withRawTypeFix(
    env: XProcessingEnv
): com.squareup.javapoet.TypeName {
  if (env.backend == XProcessingEnv.Backend.JAVAC) {
    return when (this) {
      is ParameterizedTypeName -> {
        val args = this.typeArguments.map { it.withRawTypeFix(env) }.toTypedArray()
        ParameterizedTypeName.get(this.rawType, *args)
      }
      is com.squareup.javapoet.ClassName -> {
        val mirror = env.findType(this.toString())
        if (mirror != null && mirror.typeArguments.isNotEmpty() && "<" !in this.toString()) {
          val args =
              mirror
                  .typeArguments
                  .map { WildcardTypeName.subtypeOf(com.squareup.javapoet.TypeName.OBJECT) }
                  .toTypedArray()
          ParameterizedTypeName.get(this, *args)
        } else {
          this
        }
      }
      else -> this
    }
  }
  return this
}

/**
 * Used to find equivalence of two XType since MoreTypes.equivalence() only applies to the Javac
 * backend.
 */
fun XType.isEquivalent(other: XType, env: XProcessingEnv): Boolean {
  return when (env.backend) {
    XProcessingEnv.Backend.JAVAC -> {
      val key = MoreTypes.equivalence().wrap(this.toJavac())
      val otherKey = MoreTypes.equivalence().wrap(other.toJavac())
      key == otherKey
    }
    XProcessingEnv.Backend.KSP -> {
      if ("NonExistentClass" in typeName.toString() ||
          "NonExistentClass" in other.typeName.toString()) {
        this.qualifiedName(env) == other.qualifiedName(env)
      } else {
        this.typeName.removeWildcardTypeIfContains() ==
            other.typeName.removeWildcardTypeIfContains()
      }
    }
  }
}

/**
 * Used to find the hash of an XType since MoreTypes.equivalence().hashCode() only applies to the
 * Javac backend.
 */
fun XType.hash(): Int {
  return try {
    MoreTypes.equivalence().wrap(this.toJavac()).hashCode()
  } catch (t: Throwable) {
    if (typeArguments.any { it.typeName is WildcardTypeName && it.typeName.toString() != "?" }) {
      extendsBoundOrSelf().typeName.toString().hashCode()
    } else if (this.hasCollectionType()) {
      typeName.removeWildcardTypeIfContains().toString().hashCode()
    } else {
      hashCode()
    }
  }
}

fun XType.isInternal(): Boolean {
  val ksType =
      try {
        Class.forName("androidx.room.compiler.processing.ksp.KspType")
            ?.getDeclaredField("ksType")
            ?.apply { isAccessible = true }
            ?.get(this) as?
            KSType
      } catch (throwable: Throwable) {
        null
      }
  val isTypeInternal = ksType?.declaration?.isInternal() ?: false
  return isTypeInternal || typeArguments.any { it.isInternal() }
}

fun KSType.isRaw(): Boolean {
  // yes this is gross but KSP itself seems to be doing it as well
  // https://github.com/google/ksp/blob/main/compiler-plugin/
  // src/main/kotlin/com/google/devtools/ksp/symbol/impl/kotlin/KSTypeImpl.kt#L85
  return toString().startsWith("raw ")
}

fun XType.makeNonNullByDefault(): XType {
  return try {
    if (nullability == XNullability.UNKNOWN) makeNonNullable() else this
  } catch (e: IllegalStateException) {
    // Workaround for tests since we can't call XType#nullibility for types
    // created with TypeMirror#toXProcessing(XProcessingEnv)
    this
  }
}

@OptIn(KspExperimental::class)
fun XType.mapToJavaType(env: XProcessingEnv): XType {
  if (env.backend == XProcessingEnv.Backend.KSP) {
    val resolver = env.resolver() ?: return this
    val mappedType =
        if (this.typeArguments.isEmpty()) {
          env.resolver()
              ?.getJavaClassByName(toKS().declaration.qualifiedName?.asString().orEmpty())
              ?.toXProcessing(env)
              ?.type
              ?: return this
        } else {
          val rawTypeName =
              resolver.mapKotlinNameToJava(
                  typeElement?.type?.toKS()?.declaration?.qualifiedName?.asString().orEmpty())
          val rawType =
              env.resolver()?.getJavaClassByName(rawTypeName)?.toXProcessing(env) ?: return this
          val types =
              typeArguments
                  .map {
                    val typeArgName =
                        resolver.mapKotlinNameToJava(
                            it.toKS().declaration.qualifiedName?.asString().orEmpty())
                    env.resolver()?.getJavaClassByName(typeArgName)?.toXProcessing(env)?.type
                        ?: return this
                  }
                  .toTypedArray()
          env.getDeclaredType(rawType, *types)
        }
    if ("NonExistentClass" !in mappedType.toString()) {
      return mappedType
    }
  }
  return this
}

fun XType.mapToKotlinType(env: XProcessingEnv): XType {
  if (env.backend == XProcessingEnv.Backend.KSP) {
    val resolver = env.resolver() ?: return this
    val rawTypeName = resolver.mapJavaNameToKotlin(rawType.toString())
    val mappedType =
        if (this.typeArguments.isEmpty()) {
          env.findType(rawTypeName) ?: return this
        } else {
          val rawType = env.findTypeElement(rawTypeName) ?: return this
          val types =
              typeArguments
                  .map {
                    val typeArgName = resolver.mapJavaNameToKotlin(it.qualifiedName(env))
                    env.findType(typeArgName) ?: return this
                  }
                  .toTypedArray()
          env.getDeclaredType(rawType, *types)
        }
    if ("NonExistentClass" !in mappedType.toString()) {
      return mappedType
    }
  }
  return this
}

@OptIn(KspExperimental::class)
internal fun Resolver.mapKotlinNameToJava(qualifiedName: String): String {
  val ksName = this.getKSNameFromString(qualifiedName)
  return mapKotlinNameToJava(ksName)?.asString() ?: qualifiedName
}

@OptIn(KspExperimental::class)
internal fun Resolver.mapJavaNameToKotlin(qualifiedName: String): String {
  val ksName = this.getKSNameFromString(qualifiedName)
  return mapJavaNameToKotlin(ksName)?.asString() ?: qualifiedName
}

fun XType.isDeclaredType(): Boolean {
  return typeElement?.let {
    when {
      it.isClass() -> true
      it.isInterface() -> true
      it.isEnum() -> true
      it.isAnnotationClass() -> true
      it.isKotlinObject() -> true
      else -> false
    }
  }
      ?: false
}

fun XType.isEnum(): Boolean {
  return typeElement?.let { it.isEnum() } ?: false
}

fun XType.isPrimitive(): Boolean {
  return typeName.isPrimitive
}

private fun XType.hasCollectionType(): Boolean {
  val visited = mutableSetOf<XType>()
  val queue = mutableListOf(this@hasCollectionType)
  while (queue.isNotEmpty()) {
    val type = queue.removeFirst()
    if (type in visited) continue
    if (type.typeElement?.name in collectionTypes) return true
    visited.add(type)
    queue.addAll(type.typeArguments)
  }
  return false
}

private val collectionTypes =
    setOf("MutableList", "MutableSet", "MutableCollection", "List", "Set", "Collection")
