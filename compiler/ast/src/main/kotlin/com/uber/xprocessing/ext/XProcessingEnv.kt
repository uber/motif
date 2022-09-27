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
import com.google.devtools.ksp.processing.Resolver

val XProcessingEnv.typeUtils
  get() = XTypeUtils

/** Provides access to KSP's resolver since XProcessing does not give us access */
fun XProcessingEnv.resolver(): Resolver? {
  return if (backend == XProcessingEnv.Backend.KSP) {
    Class.forName("androidx.room.compiler.processing.ksp.KspProcessingEnv")
        ?.getDeclaredField("_resolver")
        ?.apply { isAccessible = true }
        ?.get(this) as
        Resolver?
  } else {
    null
  }
}
