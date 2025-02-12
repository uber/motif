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
import androidx.room.compiler.processing.XMethodElement
import androidx.room.compiler.processing.XProcessingEnv
import androidx.room.compiler.processing.XTypeElement
import androidx.room.compiler.processing.compat.XConverters.toJavac
import com.google.common.collect.ImmutableList
import com.google.common.collect.LinkedHashMultimap
import com.google.common.collect.SetMultimap
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.toKmClass
import motif.ast.compiler.CompilerClass

/**
 * A port of MoreElements.getLocalAndInheritedMethods() using XProcessing APIs.
 * https://github.com/google/auto/blob/master/common/src/main/java/com/google/auto/common/MoreElements.java#L394
 */
fun XTypeElement.getLocalAndInheritedMethods(
    env: XProcessingEnv,
    useMoreElements: Boolean = true,
): List<XMethodElement> {
  val nonPrivateInstanceMethods =
      this.getAllNonPrivateInstanceMethods()
          .filter {
            !it.isPrivate() &&
                it.enclosingElement.toString() !in CompilerClass.TOP_LEVEL_OBJECT_NAMES
          }
          .filterNot {
            it.isPackagePrivate() &&
                it.enclosingElement.className.packageName() != this.className.packageName()
          }
  val methodMap: SetMultimap<String, XMethodElement> = LinkedHashMultimap.create()
  nonPrivateInstanceMethods.forEach { methodMap.put(it.name, it) }

  val overridden: LinkedHashSet<XMethodElement> = LinkedHashSet()
  for (methods in methodMap.asMap().values) {
    val methodList: List<XMethodElement> = ImmutableList.copyOf(methods)
    for (i in methodList.indices) {
      val methodI = methodList[i]
      for (j in i + 1 until methodList.size) {
        val methodJ = methodList[j]
        if (methodJ.overrides(methodI, this) ||
            XOverrides.overrides(methodJ, methodI, this, env, useMoreElements)) {
          overridden.add(methodI)
        }
      }
    }
  }
  val methods: MutableSet<XMethodElement> = LinkedHashSet(methodMap.values())
  methods.removeAll(overridden)
  return methods.toList()
}

/** Return where or not this XTypeElement was from an XType defined in Kotlin */
@OptIn(KotlinPoetMetadataPreview::class)
fun XTypeElement?.isKotlinSource(env: XProcessingEnv): Boolean =
    when (env.backend) {
      XProcessingEnv.Backend.JAVAC ->
          try {
            this?.toJavac()?.toKmClass() != null
          } catch (e: Throwable) {
            false
          }
      XProcessingEnv.Backend.KSP -> true
    }
