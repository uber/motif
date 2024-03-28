/*
 * Copyright (c) 2023 Uber Technologies, Inc.
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
package testcases.KT008_funky_names

import motif.Expose


@motif.Scope
interface Scope {

    fun child(): ChildScope

    fun myByte(): Byte

    fun myShort(): Short

    fun myInt(): Int

    fun myLong(): Long

    fun myFloat(): Float

    fun myDouble(): Double

    fun myBoolean(): Boolean

    fun myChar(): Char

    fun myAny(): Any

    @motif.Objects
    open class Objects {

        @Expose
        fun function2(): ((Long, Double) -> Short) = { _, d -> d.toInt().toShort() }

        fun myByte(): Byte = 3

        fun myShort(): Short = 3

        fun myInt(): Int = 3

        fun myLong(): Long = 3

        fun myFloat(): Float = 0.2f

        fun myDouble(): Double = 0.2

        fun myBoolean(): Boolean = true

        fun myChar(): Char = 'c'

        fun myAny(): Any = Any()
    }
}