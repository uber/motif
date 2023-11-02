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
package motif.compiler

import androidx.room.compiler.processing.XProcessingEnv
import motif.ast.IrClass
import motif.ast.IrType
import motif.ast.compiler.CompilerAnnotation
import motif.ast.compiler.CompilerClass
import motif.ast.compiler.CompilerMethod
import motif.ast.compiler.CompilerType
import motif.core.ResolvedGraph
import motif.core.ScopeEdge
import motif.internal.Constants
import motif.models.AccessMethodSink
import motif.models.BasicFactoryMethod
import motif.models.BindsFactoryMethod
import motif.models.ChildMethod
import motif.models.ConstructorFactoryMethod
import motif.models.FactoryMethod
import motif.models.FactoryMethodSink
import motif.models.Scope
import motif.models.Sink
import motif.models.Spread
import motif.models.Type

class ScopeImplFactory
private constructor(private val env: XProcessingEnv, private val graph: ResolvedGraph) {

  private val scopeImplClassNames = mutableMapOf<Scope, ClassName>()
  private val dependenciesClassNames = mutableMapOf<Scope, ClassName>()
  private val objectsClassNames = mutableMapOf<Scope, ClassName?>()
  private val objectsImplClassNames = mutableMapOf<Scope, ClassName>()
  private val typeNames = mutableMapOf<CompilerType, TypeName>()

  private val dependencyMethods = mutableMapOf<Scope, List<DependencyMethodData>>()

  private fun create(): List<ScopeImpl> =
      graph.scopes
          .filter { scope -> env.findTypeElement(scope.implClassName.j.toString()) == null }
          .map { scope -> Factory(scope).create() }

  private inner class Factory(private val scope: Scope) {

    private val methodNameScope = NameScope(blacklist = scope.clazz.methods.map { it.name })
    private val fieldNameScope =
        NameScope(blacklist = listOf(OBJECTS_FIELD_NAME, DEPENDENCIES_FIELD_NAME))

    private val providerMethodNames = mutableMapOf<Type, String>()
    private val cacheFieldNames = mutableMapOf<Type, String>()

    fun create(): ScopeImpl {
      val isInternal = (scope.clazz as? CompilerClass)?.isInternal() ?: false
      return ScopeImpl(
          scope.implClassName,
          scope.typeName,
          isInternal,
          scopeImplAnnotation(),
          objectsField(),
          dependenciesField(),
          cacheFields(),
          constructor(),
          alternateConstructor(),
          accessMethodImpls(),
          childMethodImpls(),
          scopeProviderMethod(),
          factoryProviderMethods(),
          dependencyProviderMethods(),
          objectsImpl(),
          dependencies())
    }

    private fun scopeImplAnnotation(): ScopeImplAnnotation {
      val childClassNames = graph.getChildEdges(scope).map { childEdge -> childEdge.child.typeName }
      return ScopeImplAnnotation(childClassNames, scope.typeName, scope.dependenciesClassName)
    }

    private fun objectsField(): ObjectsField? {
      val objectsClassName = scope.objectsClassName ?: return null
      val objectsImplClassName = scope.objectsImplClassName
      return ObjectsField(objectsClassName, objectsImplClassName, OBJECTS_FIELD_NAME)
    }

    private fun dependenciesField(): DependenciesField {
      return DependenciesField(scope.dependenciesClassName, DEPENDENCIES_FIELD_NAME)
    }

    private fun cacheFields(): List<CacheField> {
      return scope.factoryMethods.filter { it.isCached }.map { factoryMethod ->
        CacheField(getCacheFieldName(factoryMethod.returnType.type))
      }
    }

    private fun constructor(): Constructor {
      return Constructor(scope.dependenciesClassName, "dependencies", DEPENDENCIES_FIELD_NAME)
    }

    private fun alternateConstructor(): AlternateConstructor? {
      if (getDependencyMethodData(scope).isNotEmpty() ||
          !scope.dependencies?.methods.isNullOrEmpty()) {
        return null
      }
      return AlternateConstructor(scope.dependenciesClassName)
    }

    private fun accessMethodImpls(): List<AccessMethodImpl> {
      return scope.accessMethods.map { accessMethod ->
        AccessMethodImpl(
            env,
            accessMethod.method as CompilerMethod,
            getProviderMethodName(accessMethod.returnType))
      }
    }

    private fun childMethodImpls(): List<ChildMethodImpl> {
      return graph.getChildEdges(scope).map(this::childMethodImpl)
    }

    private fun childMethodImpl(childEdge: ScopeEdge): ChildMethodImpl {

      return ChildMethodImpl(
          childEdge.child.typeName,
          childEdge.child.implClassName,
          childEdge.method.method.name,
          childEdge.method.parameters.map(this::childMethodImplParameter),
          childDependenciesImpl(childEdge))
    }

    private fun childMethodImplParameter(
        childMethodParameter: ChildMethod.Parameter
    ): ChildMethodImplParameter {
      return ChildMethodImplParameter(
          childMethodParameter.parameter.type.typeName, childMethodParameter.parameter.name)
    }

    private fun childDependenciesImpl(childEdge: ScopeEdge): ChildDependenciesImpl {
      val parameters: Map<Type, ChildMethod.Parameter> =
          childEdge.method.parameters.associateBy { parameter -> parameter.type }
      val dependencyMethodImpls =
          getDependencyMethodData(childEdge.child).map { methodData ->
            childDependencyMethodImpl(parameters, methodData)
          }
      val isAbstractClass = dependencyMethodImpls.any { it.isInternal }
      return ChildDependenciesImpl(
          childEdge.child.dependenciesClassName, dependencyMethodImpls, isAbstractClass, env)
    }

    private fun childDependencyMethodImpl(
        parameters: Map<Type, ChildMethod.Parameter>,
        methodData: DependencyMethodData
    ): ChildDependencyMethodImpl {
      val parameter = parameters[methodData.returnType]
      val returnExpression =
          if (parameter == null) {
            ChildDependencyMethodImpl.ReturnExpression.Provider(
                scope.implClassName, getProviderMethodName(methodData.returnType))
          } else {
            ChildDependencyMethodImpl.ReturnExpression.Parameter(parameter.parameter.name)
          }
      val isInternal = (methodData.returnType.type as? CompilerType)?.isInternal() ?: false
      return ChildDependencyMethodImpl(
          methodData.name, methodData.returnTypeName, returnExpression, isInternal)
    }

    private fun scopeProviderMethod(): ScopeProviderMethod {
      val name = getProviderMethodName(Type(scope.clazz.type, null))
      val isInternal = (scope.clazz.type as? CompilerType)?.isInternal() ?: false
      return ScopeProviderMethod(name, scope.typeName, isInternal)
    }

    private fun factoryProviderMethods(): List<FactoryProviderMethod> {
      return scope.factoryMethods.map { factoryMethod ->
        val returnType = factoryMethod.returnType.type
        val spreadProviderMethods =
            factoryMethod.spread?.let { spreadProviderMethods(it) } ?: emptyList()
        FactoryProviderMethod(
            getProviderMethodName(returnType),
            returnType.type.typeName,
            factoryProviderMethodBody(factoryMethod),
            spreadProviderMethods,
            env)
      }
    }

    private fun factoryProviderMethodBody(factoryMethod: FactoryMethod): FactoryProviderMethodBody {
      val instantiation =
          when (factoryMethod) {
            is BasicFactoryMethod -> basicInstantiation(factoryMethod)
            is ConstructorFactoryMethod -> constructorInstantiation(factoryMethod)
            is BindsFactoryMethod -> bindsInstantiation(factoryMethod)
          }
      return if (factoryMethod.isCached) {
        FactoryProviderMethodBody.Cached(
            getCacheFieldName(factoryMethod.returnType.type),
            factoryMethod.returnType.type.type.typeName,
            instantiation,
            env)
      } else {
        FactoryProviderMethodBody.Uncached(instantiation)
      }
    }

    private fun spreadProviderMethods(spread: Spread): List<SpreadProviderMethod> {
      return spread.methods.map { method ->
        SpreadProviderMethod(
            getProviderMethodName(method.returnType),
            method.method.isStatic(),
            method.returnType.type.typeName,
            method.sourceType.type.typeName,
            getProviderMethodName(method.sourceType),
            method.name)
      }
    }

    private fun basicInstantiation(
        factoryMethod: BasicFactoryMethod
    ): FactoryProviderInstantiation.Basic {
      return FactoryProviderInstantiation.Basic(
          OBJECTS_FIELD_NAME,
          factoryMethod.objects.clazz.typeName,
          factoryMethod.isStatic,
          factoryMethod.name,
          callProviders(factoryMethod))
    }

    private fun constructorInstantiation(
        factoryMethod: ConstructorFactoryMethod
    ): FactoryProviderInstantiation.Constructor {
      return FactoryProviderInstantiation.Constructor(
          factoryMethod.returnType.type.type.typeName, callProviders(factoryMethod))
    }

    private fun bindsInstantiation(
        factoryMethod: BindsFactoryMethod
    ): FactoryProviderInstantiation.Binds {
      return FactoryProviderInstantiation.Binds(
          getProviderMethodName(factoryMethod.parameters.single().type))
    }

    private fun callProviders(factoryMethod: FactoryMethod): CallProviders {
      val names =
          factoryMethod.parameters.map { parameter -> getProviderMethodName(parameter.type) }
      return CallProviders(names)
    }

    private fun dependencyProviderMethods(): List<DependencyProviderMethod> {
      return getDependencyMethodData(scope).map { methodData ->
        DependencyProviderMethod(
            getProviderMethodName(methodData.returnType),
            methodData.returnTypeName,
            DEPENDENCIES_FIELD_NAME,
            methodData.name,
            env)
      }
    }

    private fun objectsImpl(): ObjectsImpl? {
      val objects = scope.objects ?: return null
      val objectsClassName = scope.objectsClassName ?: return null
      val abstractMethods =
          objects.factoryMethods.filter { it.method.isAbstract() }.map {
            ObjectsAbstractMethod(env, it.method as CompilerMethod)
          }
      return ObjectsImpl(
          scope.objectsImplClassName,
          objectsClassName,
          objects.clazz.kind == IrClass.Kind.INTERFACE,
          abstractMethods)
    }

    private fun dependencies(): Dependencies? {
      if (scope.dependencies != null) {
        return null
      }
      val methods =
          getDependencyMethodData(scope).map { methodData ->
            val qualifier =
                methodData.returnType.qualifier?.let { annotation ->
                  Qualifier(annotation as CompilerAnnotation)
                }
            val isInternal = (methodData.returnType.type as? CompilerType)?.isInternal() ?: false
            DependencyMethod(
                methodData.name,
                methodData.returnTypeName,
                qualifier,
                javaDoc(methodData),
                isInternal)
          }
      return Dependencies(scope.dependenciesClassName, methods)
    }

    private fun javaDoc(methodData: DependencyMethodData): DependencyMethodJavaDoc {
      val requestedFrom =
          methodData.sinks.map { sink ->
            val (ownerType, callerMethod) =
                when (sink) {
                  is FactoryMethodSink -> Pair(sink.parameter.owner.type, sink.parameter.method)
                  is AccessMethodSink -> Pair(sink.scope.clazz.type, sink.accessMethod.method)
                }

            val owner = removeGenerics(ownerType.qualifiedName)
            val methodName =
                if (callerMethod.isConstructor) ownerType.simpleName.substringBefore('<')
                else callerMethod.name
            val paramList = callerMethod.parameters.map { removeGenerics(it.type.qualifiedName) }

            JavaDocMethodLink(owner, methodName, paramList)
          }
      return DependencyMethodJavaDoc(requestedFrom)
    }

    private fun removeGenerics(name: String): String {
      return name.takeWhile { it != '<' }
    }

    private fun getProviderMethodName(type: Type): String {
      // val key = getTypeOrMappedType(type, providerMethodNames.keys)
      return providerMethodNames.computeIfAbsent(type) { methodNameScope.name(type) }
    }

    private fun getCacheFieldName(type: Type) =
        cacheFieldNames.computeIfAbsent(type) { fieldNameScope.name(type) }

    private fun getTypeOrMappedType(type: Type, keys: Set<Type>): Type {
      if (type in keys) return type

      if (type.type is CompilerType) {
        val cType = type.type as CompilerType
        val javaType = Type(cType.mapToJavaType(), type.qualifier)
        if (javaType in keys) return javaType

        val kotlinType = Type(cType.mapToKotlinType(), type.qualifier)
        if (kotlinType in keys) return kotlinType
      }

      return type
    }
  }

  private class DependencyMethodData(
      val name: String,
      val returnTypeName: TypeName,
      val returnType: Type,
      val sinks: List<Sink>
  )

  private fun getDependencyMethodData(scope: Scope): List<DependencyMethodData> {
    return dependencyMethods.computeIfAbsent(scope) { createDependencyMethods(scope) }
  }

  private fun createDependencyMethods(scope: Scope): List<DependencyMethodData> {
    val nameScope = NameScope()
    fun getName(type: Type): String {
      val dependencies = scope.dependencies ?: return nameScope.name(type)
      val method =
          dependencies.methodByType[type]
              ?: throw IllegalStateException("Could not find Dependencies method for type: $type")
      return method.method.name
    }
    return graph.getUnsatisfied(scope).toSortedMap().entries.map { (type, sinks) ->
      DependencyMethodData(getName(type), type.type.typeName, type, sinks)
    }
  }

  private val IrType.typeName: TypeName
    get() = typeNames.computeIfAbsent(this as CompilerType) { type -> TypeName.get(type.mirror) }

  private val IrClass.typeName: ClassName
    get() = type.typeName.className

  private val Scope.typeName: ClassName
    get() = clazz.typeName

  private val Scope.implClassName: ClassName
    get() =
        scopeImplClassNames.computeIfAbsent(this) { scope ->
          val scopeClassName = scope.clazz.typeName
          val prefix = scopeClassName.kt.simpleNames.joinToString("")
          ClassName.get(scopeClassName.kt.packageName, "$prefix${Constants.SCOPE_IMPL_SUFFIX}")
        }

  private val Scope.dependenciesClassName: ClassName
    get() =
        dependenciesClassNames.computeIfAbsent(this) {
          dependencies?.clazz?.typeName ?: implClassName.nestedClass("Dependencies")
        }

  private val Scope.objectsClassName: ClassName?
    get() =
        objectsClassNames.computeIfAbsent(this) { scope ->
          val objects = scope.objects ?: return@computeIfAbsent null
          objects.clazz.typeName
        }

  private val Scope.objectsImplClassName: ClassName
    get() =
        objectsImplClassNames.computeIfAbsent(this) { scope ->
          scope.implClassName.nestedClass("Objects")
        }

  companion object {

    private const val OBJECTS_FIELD_NAME = "objects"
    private const val DEPENDENCIES_FIELD_NAME = "dependencies"

    fun create(env: XProcessingEnv, graph: ResolvedGraph): List<ScopeImpl> {
      return ScopeImplFactory(env, graph).create()
    }
  }
}
