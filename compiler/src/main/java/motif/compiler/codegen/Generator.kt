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
package motif.compiler.codegen

import motif.models.graph.Graph
import motif.models.graph.Scope
import javax.annotation.processing.ProcessingEnvironment

class Generator(
        env: ProcessingEnvironment,
        private val graph: Graph) : CodegenCache(env, CacheScope()) {

    private val scopeImplFactory = ScopeImplFactory(env, cacheScope, graph)

    fun generate() {
        graph.scopes
                .filter {
                    !it.isImplGenerated()
                }
                .forEach {
                    scopeImplFactory.create(it)
                }
    }

    private fun Scope.isImplGenerated(): Boolean {
        val scopeImplName = implTypeName.qualifiedName()
        return env.elementUtils.getTypeElement(scopeImplName) != null
    }
}