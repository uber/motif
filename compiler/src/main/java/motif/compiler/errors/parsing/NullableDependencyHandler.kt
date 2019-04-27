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
package motif.compiler.errors.parsing

import motif.compiler.errors.ErrorHandler
import motif.ast.compiler.CompilerMethod
import motif.models.errors.NullableDependency
import javax.lang.model.element.Element

class NullableDependencyHandler : ErrorHandler<NullableDependency>() {

    override fun message(error: NullableDependency): String {
        return "Motif dependencies may not be nullable. Consider using Optional<...> instead."
    }

    override fun element(error: NullableDependency): Element? {
        return (error.method as CompilerMethod).element
    }
}