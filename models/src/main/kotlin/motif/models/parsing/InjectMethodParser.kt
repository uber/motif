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
package motif.models.parsing

import motif.models.java.IrMethod
import motif.models.motif.inject.InjectMethod

class InjectMethodParser : ParserUtil {

    fun isApplicable(method: IrMethod): Boolean {
        return method.parameters.size == 1 && (
                method.isVoid() || method.returnType == method.parameters[0].type
        )
    }

    fun parse(method: IrMethod): InjectMethod {
        return InjectMethod(method, method.parameters[0])
    }
}
