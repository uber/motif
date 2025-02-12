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
import androidx.room.compiler.processing.XAnnotation
import androidx.room.compiler.processing.XAnnotationValue
import androidx.room.compiler.processing.XProcessingEnv
import androidx.room.compiler.processing.compat.XConverters.toJavac
import com.google.auto.common.AnnotationMirrors

/**
 * Used to find equivalence of two XAnnotation since AnnotationMirrors.equivalence() only applies to
 * the Javac backend.
 */
fun XAnnotation.isEquivalent(other: XAnnotation, env: XProcessingEnv): Boolean =
    if (env.backend == XProcessingEnv.Backend.JAVAC) {
      val key = AnnotationMirrors.equivalence().wrap(this.toJavac())
      val otherKey = AnnotationMirrors.equivalence().wrap(other.toJavac())
      key == otherKey
    } else {
      (type.isEquivalent(other.type, env) &&
          annotationValues.size == other.annotationValues.size &&
          annotationValues.zip(other.annotationValues).all { (lhs, rhs) -> lhs.isEquivalent(rhs) })
    }

/**
 * Used to find equivalence of two XAnnotation since AnnotationMirrors.equivalence() only applies to
 * the Javac backend.
 */
fun XAnnotationValue.isEquivalent(other: XAnnotationValue): Boolean =
    this.name == other.name && this.value == other.value

/** Cleans up differences in the toString() methods between the JAVAC and KSP backends. */
fun XAnnotation.toPrettyString(): String {
  val annotationValues =
      this.annotationValues
          .map {
            if (it.name == "value") {
              "\"${it.value}\""
            } else {
              "${it.name} = ${it.value}"
            }
          }
          .joinToString(", ")
  val annotationValuesList =
      if (this.annotationValues.isNotEmpty()) {
        "($annotationValues)"
      } else {
        ""
      }
  return "@${this.qualifiedName}$annotationValuesList"
}
