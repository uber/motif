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

import motif.ast.IrClass
import motif.ast.IrType

/** [Wiki](https://github.com/uber/motif/wiki#scope) */
sealed class Scope(val useNullFieldInitialization : Boolean, val clazz: IrClass) {
//rubin123
  val source by lazy { ScopeSource(this) }
  val simpleName: String by lazy { clazz.simpleName }
  val qualifiedName: String by lazy { clazz.qualifiedName }

  abstract val objects: Objects?
  abstract val accessMethods: List<AccessMethod>
  abstract val childMethods: List<ChildMethod>
  abstract val factoryMethods: List<FactoryMethod>
  abstract val dependencies: Dependencies?

  companion object {

    fun fromClasses(scopeClasses: List<IrClass>): List<Scope> = ScopeFactory(scopeClasses).create()
  }
}

class ErrorScope internal constructor(clazz: IrClass, val parsingError: ParsingError) :
    Scope(false, clazz) {
  override val objects: Objects? = null
  override val accessMethods: List<AccessMethod> = emptyList()
  override val childMethods: List<ChildMethod> = emptyList()
  override val factoryMethods: List<FactoryMethod> = emptyList()
  override val dependencies: Dependencies? = null
}

class ValidScope internal constructor(clazz: IrClass, useNullFieldInitialization: Boolean = false) : Scope(useNullFieldInitialization, clazz) {

  init {
    if (clazz.kind != IrClass.Kind.INTERFACE) throw ScopeMustBeAnInterface(clazz)
    detectScopeSuperinterface(this)
  }

  override val objects: Objects? = Objects.fromScope(this)

  private val scopeMethods =
      clazz.methods.map { method -> ScopeMethod.fromScopeMethod(this, method) }

  override val accessMethods: List<AccessMethod> =
      scopeMethods.mapNotNull { method -> method as? AccessMethod }

  override val childMethods: List<ChildMethod> =
      scopeMethods.mapNotNull { method -> method as? ChildMethod }

  override val factoryMethods: List<FactoryMethod> = objects?.factoryMethods ?: emptyList()

  override val dependencies: Dependencies? = Dependencies.fromScope(this)

  companion object {

    private fun detectScopeSuperinterface(scope: Scope, clazz: IrClass = scope.clazz) {
      clazz.supertypes.forEach { superType ->
        superType.resolveClass()?.let { superClass ->
          if (superClass.hasAnnotation(motif.Scope::class)) {
            throw ScopeExtendsScope(scope)
          }
          detectScopeSuperinterface(scope, superClass)
        }
      }
    }
  }
}

private class ScopeFactory(private val initialScopeClasses: List<IrClass>) {

  private val scopeMap: MutableMap<IrType, Scope> = mutableMapOf()
  private val visited: MutableSet<IrType> = mutableSetOf()

  fun create(): List<Scope> {
    initialScopeClasses.forEach(this::visit)
    return scopeMap.values.toList()
  }

  private fun visit(scopeClass: IrClass) {
    val scopeType = scopeClass.type

    if (visited.contains(scopeType)) return

    visited.add(scopeType)

    if (!scopeMap.containsKey(scopeType)) {
      val scope =
          try {
            ValidScope(scopeClass)
          } catch (e: ParsingError) {
            ErrorScope(scopeClass, e)
          }
      scope.childMethods.forEach { childMethod -> visit(childMethod.childScopeClass) }
      scopeMap[scopeType] = scope
    }
  }
}
