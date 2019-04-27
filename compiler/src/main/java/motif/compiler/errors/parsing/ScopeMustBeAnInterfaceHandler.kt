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
import motif.ast.compiler.CompilerClass
import motif.models.errors.ScopeMustBeAnInterface
import javax.lang.model.element.Element

class ScopeMustBeAnInterfaceHandler : ErrorHandler<ScopeMustBeAnInterface>() {

    override fun message(error: ScopeMustBeAnInterface): String {
        return "Motif Scopes must be interfaces."
    }

    override fun element(error: ScopeMustBeAnInterface): Element? {
        return (error.scopeClass as CompilerClass).declaredType.asElement()
    }
}