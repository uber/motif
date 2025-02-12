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
package motif.core

import java.util.Stack

/**
 * Finds and returns the first cycle encountered given initial items and a lambda to retrieve the
 * children of an item.
 */
class Cycle<T>(val path: List<T>) {

  companion object {

    fun <T> find(items: Iterable<T>, getChildren: (T) -> Iterable<T>): Cycle<T>? =
        CycleFinder(items, getChildren).find()
  }
}

private class CycleFinder<T>(
    private val items: Iterable<T>,
    private val getChildren: (T) -> Iterable<T>,
) {

  fun find(): Cycle<T>? {
    val cyclePath = calculateCyclePath(Stack(), items) ?: return null
    return Cycle(cyclePath)
  }

  private fun calculateCyclePath(path: Stack<T>, items: Iterable<T>): List<T>? {
    items.forEach { item ->
      calculateCyclePath(path, item)?.let { cycle ->
        return cycle
      }
    }

    return null
  }

  private fun calculateCyclePath(path: Stack<T>, item: T): List<T>? {
    val seenIndex = path.indexOf(item)
    if (seenIndex != -1) {
      return path.subList(seenIndex, path.size) + item
    }

    path.push(item)

    val children = getChildren(item)

    calculateCyclePath(path, children)?.let { cycle ->
      return cycle
    }

    path.pop()

    return null
  }
}
