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
package motif.compiler.codegenv2

import motif.core.ResolvedGraph
import javax.annotation.processing.ProcessingEnvironment

object CodeGenerator {

    fun generate(env: ProcessingEnvironment, graph: ResolvedGraph) {
        ScopeImplFactory.create(env, graph)
                .map { scopeImpl -> JavaCodeGenerator.generate(scopeImpl) }
                .forEach { javaFile ->
                    javaFile.writeTo(env.filer)
                }
    }
}