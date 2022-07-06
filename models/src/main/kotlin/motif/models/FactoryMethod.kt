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

import javax.inject.Inject
import motif.DoNotCache
import motif.Expose
import motif.ast.IrClass
import motif.ast.IrMethod
import motif.ast.IrParameter

/** [Wiki](https://github.com/uber/motif/wiki#factory-methods) */
sealed class FactoryMethod(val method: IrMethod, val objects: Objects) {

  abstract val returnType: ReturnType
  abstract val parameters: List<Parameter>

  val isStatic = method.isStatic()
  val isCached = !method.hasAnnotation(DoNotCache::class)
  val isExposed = method.hasAnnotation(Expose::class)

  val spread: Spread? =
      if (method.hasAnnotation(motif.Spread::class)) {
        val returnType = method.returnType
        val returnClass =
            returnType.resolveClass() ?: throw UnspreadableType(objects, method, returnType)
        Spread(returnClass, this)
      } else {
        null
      }

  val spreadSources: List<SpreadSource> = spread?.methods?.map(::SpreadSource) ?: emptyList()
  val sources: List<Source> by lazy { spreadSources + FactoryMethodSource(this) }
  val sinks: List<FactoryMethodSink> by lazy { parameters.map(::FactoryMethodSink) }

  val name = method.name
  val qualifiedName: String by lazy { "${objects.qualifiedName}.${method.name}" }

  protected fun getParameters(owner: IrClass, method: IrMethod): List<Parameter> {
    return method.parameters.map { parameter ->
      Parameter(owner, method, parameter, this, Type.fromParameter(parameter))
    }
  }

  class Parameter(
      val owner: IrClass,
      val method: IrMethod,
      val parameter: IrParameter,
      val factoryMethod: FactoryMethod,
      val type: Type
  ) {

    val qualifiedName: String by lazy { type.qualifiedName }
  }

  class ReturnType(val factoryMethod: FactoryMethod, val type: Type) {

    val qualifiedName: String by lazy { type.qualifiedName }
  }

  companion object {

    fun fromObjectsMethod(objects: Objects, method: IrMethod): FactoryMethod {
      if (method.isVoid()) throw VoidFactoryMethod(objects, method)
      if (method.isNullable()) throw NullableFactoryMethod(objects, method)

      ensureNonNullParameters(objects.scope, objects.clazz, method)

      if (!method.isAbstract()) return BasicFactoryMethod.create(objects, method)
      if (!method.hasParameters()) return ConstructorFactoryMethod.create(objects, method)
      if (method.parameters.size == 1) return BindsFactoryMethod.create(objects, method)

      throw InvalidFactoryMethod(objects, method)
    }
  }
}

/** [Wiki](https://github.com/uber/motif/wiki#basic) */
class BasicFactoryMethod private constructor(objects: Objects, method: IrMethod) :
    FactoryMethod(method, objects) {

  override val returnType = ReturnType(this, Type.fromReturnType(method))
  override val parameters = getParameters(objects.clazz, method)

  companion object {

    fun create(objects: Objects, method: IrMethod): BasicFactoryMethod {
      return BasicFactoryMethod(objects, method)
    }
  }
}

/** [Wiki](https://github.com/uber/motif/wiki#constructor) */
class ConstructorFactoryMethod private constructor(objects: Objects, method: IrMethod) :
    FactoryMethod(method, objects) {

  override val returnType = ReturnType(this, Type.fromReturnType(method))
  override val parameters =
      {
        val returnType = method.returnType
        val returnClass: IrClass =
            returnType.resolveClass() ?: throw NoSuitableConstructor(objects, method, returnType)

        if (returnClass.isAbstract()) {
          throw NoSuitableConstructor(objects, method, returnType)
        }

        val constructors: List<IrMethod> = returnClass.constructors

        if (constructors.isEmpty()) {
          emptyList()
        } else {
          val constructor =
              if (constructors.size == 1) {
                constructors[0]
              } else {
                constructors.find { it.hasAnnotation(Inject::class) }
                    ?: throw InjectAnnotationRequired(objects, method, returnType)
              }

          ensureNonNullParameters(objects.scope, returnClass, constructor)

          getParameters(returnClass, constructor)
        }
      }()

  companion object {

    fun create(objects: Objects, method: IrMethod): ConstructorFactoryMethod {
      return ConstructorFactoryMethod(objects, method)
    }
  }
}

/** [Wiki](https://github.com/uber/motif/wiki#constructor) */
class BindsFactoryMethod private constructor(objects: Objects, method: IrMethod) :
    FactoryMethod(method, objects) {

  override val returnType = ReturnType(this, Type.fromReturnType(method))
  override val parameters = getParameters(objects.clazz, method)

  companion object {

    fun create(objects: Objects, method: IrMethod): BindsFactoryMethod {
      val returnType = Type.fromReturnType(method)
      val parameterType = Type.fromParameter(method.parameters[0])
      if (!parameterType.type.isAssignableTo(returnType.type)) {
        throw NotAssignableBindsMethod(objects, method, returnType.type, parameterType.type)
      }
      return BindsFactoryMethod(objects, method)
    }
  }
}

private fun ensureNonNullParameters(scope: Scope, owner: IrClass, method: IrMethod) {
  method.parameters.forEach { parameter -> ensureNonNullParameter(scope, owner, method, parameter) }
}

private fun ensureNonNullParameter(
    scope: Scope,
    owner: IrClass,
    method: IrMethod,
    parameter: IrParameter
) {
  if (parameter.isNullable()) throw NullableParameter(scope, owner, method, parameter)
}
