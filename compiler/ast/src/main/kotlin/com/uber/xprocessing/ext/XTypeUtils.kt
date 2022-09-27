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
import androidx.room.compiler.processing.XProcessingEnv
import androidx.room.compiler.processing.XType

/** Provides additional utils that the JAVAC backend usually gets from TypeUtils. */
object XTypeUtils {
  fun isAssignable(t1: XType, t2: XType) = t2.isAssignableFrom(t1)

  fun isSameType(t1: XType, t2: XType) = t1.isSameType(t2)

  fun directSupertypes(t: XType) = t.superTypes

  fun erasure(t: XType, env: XProcessingEnv) =
      if (t.typeArguments.isNotEmpty()) {
        env.findTypeElement(t.rawType.toString())?.type
        t.typeElement?.let { env.getDeclaredType(it) } ?: t
      } else {
        t
      }
}
