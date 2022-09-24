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
package motif.compiler.ksp

import com.google.devtools.ksp.KspErrorThrower
import javax.tools.Diagnostic
import motif.compiler.processing.XAnnotation
import motif.compiler.processing.XAnnotationValue
import motif.compiler.processing.XElement
import motif.compiler.processing.XMessager

/**
 * A message watcher that re-throws exceptions from the KSP devtools package to work around an issue
 * where exceptions are swallowed.
 *
 * https://github.com/google/ksp/issues/974
 */
internal class MessageWatcher : XMessager() {
  override fun onPrintMessage(
      kind: Diagnostic.Kind,
      msg: String,
      element: XElement?,
      annotation: XAnnotation?,
      annotationValue: XAnnotationValue?
  ) {
    if (kind == Diagnostic.Kind.ERROR) {
      KspErrorThrower.rethrowKspError(msg)
    }
  }
}
