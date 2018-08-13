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

import javax.lang.model.element.Element

// TODO Make this a sealed class. Wherever we throw a ParsingError, convert to a subclass of this
// class. For instance, we throw a ParsingError when we find multiple qualifiers on the same method.
// We should instead throw a more specific subclass "MultipleQualifiersError : ParsingError". We can
// then add tests for these cases in the same way we test for GraphValidationErrors.
class ParsingError(val element: Element, override val message: String) : RuntimeException(message)