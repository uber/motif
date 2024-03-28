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
package motif.compiler

import androidx.room.compiler.processing.XAnnotation
import androidx.room.compiler.processing.XType
import com.google.common.annotations.VisibleForTesting
import java.util.Locale
import motif.ast.compiler.CompilerAnnotation
import motif.ast.compiler.CompilerType
import motif.models.Type

class NameScope(blacklist: Iterable<String> = emptySet()) {

  private val names = UniqueNameSet(blacklist)

  fun name(type: Type): String {
    return names.unique(
        Names.safeName(
            (type.type as CompilerType).mirror, (type.qualifier as? CompilerAnnotation)?.mirror))
  }
}

private class UniqueNameSet(blacklist: Iterable<String>) {

  private val used: MutableSet<String> = blacklist.toMutableSet()

  fun unique(base: String): String {
    var name = base
    var i = 2
    while (!used.add(name)) {
      name = "$base${i++}"
    }
    return name
  }
}

object Names {

  @JvmStatic
  @VisibleForTesting
  fun safeName(typeMirror: XType, annotation: XAnnotation?): String {
    val name = XNameVisitor.visit(typeMirror)
    val annotationString = annotationString(annotation)
    return "$annotationString${name}_".replaceFirstChar { it.lowercase(Locale.ENGLISH) }
  }

  private fun annotationString(annotation: XAnnotation?): String {
    return if (annotation?.qualifiedName == "javax.inject.Named") {
      annotation.getAnnotationValue("value").value.toString()
    } else {
      annotation?.type?.typeElement?.name.orEmpty()
    }
  }
}
