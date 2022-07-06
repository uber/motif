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
package motif.errormessage

import motif.models.AccessMethodSink
import motif.models.ChildParameterSource
import motif.models.FactoryMethodSink
import motif.models.FactoryMethodSource
import motif.models.Node
import motif.models.ScopeSource
import motif.models.SpreadSource

object NodeHandler {

  fun handle(node: Node): String {
    return when (node) {
      is ScopeSource -> {
        """[SCOPE]
                    |  TYPE: ${node.scope.qualifiedName}""".trimMargin()
      }
      is FactoryMethodSource -> {
        """[FACTORY METHOD RETURN TYPE]
                    |  TYPE:   ${node.factoryMethod.returnType.qualifiedName}
                    |  METHOD: ${node.factoryMethod.qualifiedName}""".trimMargin()
      }
      is SpreadSource -> {
        """[SPREAD METHOD]
                    |  TYPE:           ${node.spreadMethod.returnType.qualifiedName}
                    |  METHOD:         ${node.spreadMethod.spread.qualifiedName}.${node.spreadMethod.method.name}
                    |  FACTORY METHOD: ${node.spreadMethod.spread.factoryMethod.qualifiedName}
                """.trimMargin()
      }
      is ChildParameterSource -> {
        """[CHILD METHOD PARAMETER]
                    |  TYPE:      ${node.parameter.type.qualifiedName}
                    |  METHOD:    ${node.parameter.method.qualifiedName}
                    |  PARAMETER: ${node.parameter.parameter.name}
                """.trimMargin()
      }
      is FactoryMethodSink -> {
        """[FACTORY METHOD PARAMETER]
                    |  TYPE:      ${node.parameter.type.qualifiedName}
                    |  METHOD:    ${node.parameter.factoryMethod.qualifiedName}
                    |  PARAMETER: ${node.parameter.parameter.name}
                """.trimMargin()
      }
      is AccessMethodSink -> {
        """[ACCESS METHOD]
                    |  TYPE:   ${node.accessMethod.returnType.qualifiedName}
                    |  METHOD: ${node.accessMethod.qualifiedName}
                """.trimMargin()
      }
    }
  }
}
