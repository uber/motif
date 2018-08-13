/*
 * Copyright (c) 2018 Uber Technologies, Inc.
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
package motif.ir.source.base

class Annotation(
        val userData: Any?,
        val id: String) {

    override fun toString(): String {
        return id
    }

    override fun equals(other: Any?): Boolean {
        val o = other as? Annotation ?: return false
        return id == o.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}