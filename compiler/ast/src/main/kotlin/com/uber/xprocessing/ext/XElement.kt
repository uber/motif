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
package com.uber.xprocessing.ext

import androidx.room.compiler.processing.XElement
import androidx.room.compiler.processing.XHasModifiers
import androidx.room.compiler.processing.XMethodElement
import javax.lang.model.element.Modifier

val XElement.modifiers: List<Modifier>
  get() {
    return (this as? XHasModifiers)?.let {
      val modifiers = mutableListOf<Modifier>()
      if (isPublic()) modifiers += Modifier.PUBLIC
      if (isProtected()) modifiers += Modifier.PROTECTED
      if (isPrivate()) modifiers += Modifier.PRIVATE
      if (isAbstract()) modifiers += Modifier.ABSTRACT
      if (this is XMethodElement && isJavaDefault()) modifiers += Modifier.DEFAULT
      if (isStatic()) modifiers += Modifier.STATIC
      if (isFinal()) modifiers += Modifier.FINAL
      if (isTransient()) modifiers += Modifier.TRANSIENT
      return@let modifiers
    } ?: emptyList()
  }

val XElement.modifierNames: List<String>
  get() = modifiers.map { it.name }

fun XHasModifiers.isPackagePrivate(): Boolean = !isPrivate() && !isProtected() && !isPublic()
