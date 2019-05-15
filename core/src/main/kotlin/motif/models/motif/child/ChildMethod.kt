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
package motif.models.motif.child

import motif.ast.IrMethod
import motif.ast.IrType
import motif.models.motif.dependencies.DynamicDependency

class ChildMethod(
        val ir: IrMethod,
        val scope: IrType,
        val dynamicDependencies: List<DynamicDependency>)
