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
package testcases.KT006_scope_factory

import com.google.common.truth.Truth
import motif.ScopeFactory

class Test {

    companion object {
        @JvmStatic
        fun run() {
            val scope = ScopeFactory.create(Scope::class.java, object: Scope.Dependencies {
                override fun s(): String {
                    return "s"
                }
            })
            Truth.assertThat(scope.string()).isEqualTo("s")
        }
    }
}