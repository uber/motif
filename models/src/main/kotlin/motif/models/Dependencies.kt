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
package motif.models

import motif.Creatable
import motif.ast.IrClass
import motif.ast.IrMethod

/** [Wiki](https://github.com/uber/motif/wiki#dependencies) */
class Dependencies(val clazz: IrClass, val scope: Scope) {

  val methods: List<Method> =
      clazz.methods.map { method ->
        if (method.isVoid()) throw VoidDependenciesMethod(scope, clazz, method)
        if (method.hasParameters()) throw DependencyMethodWithParameters(scope, clazz, method)
        val type = Type.fromReturnType(method)
        Method(this, method, type)
      }

  val methodByType: Map<Type, Method> =
      methods
          .groupBy { it.returnType }
          .mapValues { (type, methods) ->
            if (methods.size > 1) {
              throw DuplicatedDependenciesMethod(scope, methods)
            }
            methods.first()
          }
          .toSortedMap()

  val types: List<Type> = methods.map { it.returnType }

  class Method(val dependencies: Dependencies, val method: IrMethod, val returnType: Type) {

    val qualifiedName: String by lazy { "${dependencies.clazz.qualifiedName}.${method.name}" }
  }

  companion object {

    fun fromScope(scope: Scope): Dependencies? {
      val creatable = findCreatableSuperinterface(scope.clazz) ?: return null
      val dependenciesType = creatable.typeArguments.singleOrNull() ?: return null
      val dependenciesClass = dependenciesType.resolveClass() ?: return null
      return Dependencies(dependenciesClass, scope)
    }

    private fun findCreatableSuperinterface(clazz: IrClass): IrClass? {
      if (clazz.qualifiedName.takeWhile { it != '<' } == Creatable::class.java.name) {
        return clazz
      }

      clazz.supertypes.mapNotNull { it.resolveClass() }.forEach { superinterface ->
        findCreatableSuperinterface(superinterface)?.let {
          return it
        }
      }

      return null
    }
  }
}
