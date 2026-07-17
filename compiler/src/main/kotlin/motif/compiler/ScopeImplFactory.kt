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
private constructor(
    private val env: XProcessingEnv,
    private val graph: ResolvedGraph,
) {

  private val scopeImplClassNames = mutableMapOf<Scope, ClassName>()
  private val dependenciesClassNames = mutableMapOf<Scope, ClassName>()
  private val objectsClassNames = mutableMapOf<Scope, ClassName?>()
  private val objectsImplClassNames = mutableMapOf<Scope, ClassName>()
  private val typeNames = mutableMapOf<CompilerType, TypeName>()

  private val dependencyMethods = mutableMapOf<Scope, List<DependencyMethodData>>()

  private fun create(): List<ScopeImpl> =
      graph.scopes
          .filter { scope -> env.findTypeElement(scope.implClassName.j.toString()) == null }
          .flatMap { scope -> Factory(scope).create() }

  private inner class Factory(private val scope: Scope) {

    private val methodNameScope = NameScope(blacklist = scope.clazz.methods.map { it.name })
    private val fieldNameScope =
        NameScope(blacklist = listOf(OBJECTS_FIELD_NAME, DEPENDENCIES_FIELD_NAME))

    private val providerMethodNames = mutableMapOf<Type, String>()
    private val cacheFieldNames = mutableMapOf<Type, String>()

    // Memoization for shouldCache() to avoid recomputation and handle recursion
    private val shouldCacheCache = mutableMapOf<Type, Boolean>()
    private val shouldCacheComputing = mutableSetOf<Type>()

    // Memoization for dependent lookup: Type -> Set of FactoryMethods
    // Also used for usage count via dependentsCache[type]?.size
    private val dependentsCache by lazy {
      val dependents = mutableMapOf<Type, MutableSet<FactoryMethod>>()
      scope.factoryMethods.forEach { factoryMethod ->
        factoryMethod.parameters.forEach { param ->
          dependents.getOrPut(param.type) { mutableSetOf() }.add(factoryMethod)
        }
        // Also count spread method consumers: the spread source type is consumed by the
        // factory method that produces it, so downstream spread consumers should count
        // as additional usage of the spread source's return type
        factoryMethod.spread?.methods?.forEach { spreadMethod ->
          dependents.getOrPut(spreadMethod.sourceType) { mutableSetOf() }.add(factoryMethod)
        }
      }
      dependents
    }

    fun create(): List<ScopeImpl> {
      val isInternal = (scope.clazz as? CompilerClass)?.isInternal() ?: false
      val scopeAnnotation = scope.clazz.annotations
          .find { it.className == motif.Scope::class.java.name }!!
      val cachingStrategy = resolveCachingStrategy(scopeAnnotation)

      // For RUNTIME_SELECTABLE, we generate 3 ScopeImpl instances:
      // 1. Wrapper class (isRuntimeSelectableWrapper = true, variantSuffix = null)
      // 2. BASELINE_WITH_LOCK_SELECTABLE variant (variantSuffix = "_BaselineSelectableLock")
      // 3. SMART_CACHE variant (variantSuffix = "_SmartCache")
      if (cachingStrategy == motif.CachingStrategy.RUNTIME_SELECTABLE) {
        return listOf(
            createWrapper(),
            createVariant(motif.CachingStrategy.BASELINE_WITH_LOCK_SELECTABLE, "_BaselineSelectableLock"),
            createVariant(motif.CachingStrategy.SMART_CACHE, "_SmartCache")
        )
      }

      return listOf(createVariant(cachingStrategy, variantSuffix = null))
    }

    private fun createWrapper(): ScopeImpl {
      val isInternal = (scope.clazz as? CompilerClass)?.isInternal() ?: false
      // Wrapper generates the Dependencies nested class that variants will reference
      val scopeAnnotation = scope.clazz.annotations
          .find { it.className == motif.Scope::class.java.name }!!
      val cachingStrategy = resolveCachingStrategy(scopeAnnotation)

      return ScopeImpl(
          isBaselineStrategy = true, // Not used for wrapper
          className = scope.implClassName,
          superClassName = scope.typeName,
          internalScope = isInternal,
          scopeImplAnnotation = scopeImplAnnotation(),
          objectsField = null, // Wrapper doesn't need objects field (it delegates)
          dependenciesField = dependenciesField(),
          cacheFields = emptyList(), // Wrapper doesn't have cache fields
          perDependencyLockFields = null, // Wrapper doesn't have lock fields
          constructor = constructor(),
          alternateConstructor = alternateConstructor(),
          accessMethodImpls = accessMethodImpls(),
          childMethodImpls = childMethodImpls(cachingStrategy, variantSuffix = null),
          scopeProviderMethod = scopeProviderMethod(),
          factoryProviderMethods = emptyList(), // Wrapper delegates instead
          dependencyProviderMethods = emptyList(), // Wrapper delegates instead
          objectsImpl = objectsImpl(), // Wrapper needs Objects nested class for variants to reference
          dependencies = dependencies(forceGenerate = true), // Wrapper needs Dependencies nested class for variants to reference
          isRuntimeSelectableWrapper = true,
          variantSuffix = null,
      )
    }

    private fun createVariant(cachingStrategy: motif.CachingStrategy, variantSuffix: String?): ScopeImpl {
      val isInternal = (scope.clazz as? CompilerClass)?.isInternal() ?: false
      val isBaselineStrategy = cachingStrategy == motif.CachingStrategy.BASELINE ||
                               cachingStrategy == motif.CachingStrategy.BASELINE_WITH_LOCK_SELECTABLE
      val useSelectiveCaching = cachingStrategy == motif.CachingStrategy.SMART_CACHE

      return ScopeImpl(
          isBaselineStrategy,
          scope.implClassName,
          scope.typeName,
          isInternal,
          scopeImplAnnotation(),
          objectsField(),
          dependenciesField(),
          cacheFields(useSelectiveCaching),
          perDependencyLockFields(cachingStrategy, useSelectiveCaching),
          constructor(),
          alternateConstructor(),
          accessMethodImpls(),
          childMethodImpls(cachingStrategy, variantSuffix),
          scopeProviderMethod(),
          factoryProviderMethods(useSelectiveCaching),
          dependencyProviderMethods(),
          objectsImpl(),
          dependencies(),
          isRuntimeSelectableWrapper = false,
          variantSuffix = variantSuffix,
      )
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

    private fun dependenciesField(): DependenciesField =
        DependenciesField(scope.dependenciesClassName, DEPENDENCIES_FIELD_NAME)

    private fun cacheFields(useSelectiveCaching: Boolean): List<CacheField> =
        scope.factoryMethods
            .filter { factoryMethod ->
              if (useSelectiveCaching) {
                !shouldSkipCaching(factoryMethod, useSelectiveCaching) && shouldCache(factoryMethod)
              } else {
                factoryMethod.isCached
              }
            }
            .map { factoryMethod -> CacheField(getCacheFieldName(factoryMethod.returnType.type)) }

    private fun perDependencyLockFields(cachingStrategy: motif.CachingStrategy, useSelectiveCaching: Boolean): PerDependencyLockFields? {
      // Generate per-dependency lock fields for strategies that support runtime lock selection
      // Both BASELINE_WITH_LOCK_SELECTABLE and SMART_CACHE support per-dependency locks
      if (cachingStrategy != motif.CachingStrategy.BASELINE_WITH_LOCK_SELECTABLE &&
          cachingStrategy != motif.CachingStrategy.SMART_CACHE) {
        return null
      }

      // Create a map from cache field names to lock field names
      val locks = scope.factoryMethods
          .filter { factoryMethod ->
            if (useSelectiveCaching) {
              !shouldSkipCaching(factoryMethod, useSelectiveCaching) && shouldCache(factoryMethod)
            } else {
              factoryMethod.isCached
            }
          }
          .associate { factoryMethod ->
            val cacheFieldName = getCacheFieldName(factoryMethod.returnType.type)
            val lockFieldName = "lock_$cacheFieldName"
            cacheFieldName to lockFieldName
          }

      return if (locks.isEmpty()) null else PerDependencyLockFields(locks)
    }

    private fun constructor(): Constructor =
        Constructor(scope.dependenciesClassName, "dependencies", DEPENDENCIES_FIELD_NAME)

    private fun alternateConstructor(): AlternateConstructor? {
      if (getDependencyMethodData(scope).isNotEmpty() ||
          !scope.dependencies?.methods.isNullOrEmpty()) {
        return null
      }
      return AlternateConstructor(scope.dependenciesClassName)
    }

    private fun accessMethodImpls(): List<AccessMethodImpl> =
        scope.accessMethods.map { accessMethod ->
          AccessMethodImpl(
              env,
              accessMethod.method as CompilerMethod,
              getProviderMethodName(accessMethod.returnType),
          )
        }

    private fun childMethodImpls(cachingStrategy: motif.CachingStrategy, variantSuffix: String?): List<ChildMethodImpl> =
        graph.getChildEdges(scope).map { childEdge -> childMethodImpl(childEdge, cachingStrategy, variantSuffix) }

    private fun childMethodImpl(childEdge: ScopeEdge, cachingStrategy: motif.CachingStrategy, variantSuffix: String?): ChildMethodImpl =
        ChildMethodImpl(
            childEdge.child.typeName,
            childEdge.child.implClassName,
            childEdge.method.method.name,
            childEdge.method.parameters.map(this::childMethodImplParameter),
            childDependenciesImpl(childEdge, cachingStrategy, variantSuffix),
        )

    private fun childMethodImplParameter(
        childMethodParameter: ChildMethod.Parameter,
    ): ChildMethodImplParameter =
        ChildMethodImplParameter(
            childMethodParameter.parameter.type.typeName,
            childMethodParameter.parameter.name,
        )

    private fun childDependenciesImpl(childEdge: ScopeEdge, cachingStrategy: motif.CachingStrategy, variantSuffix: String?): ChildDependenciesImpl {
      // For variants, use the variant class name (with suffix) for the parent scope class name
      // This is needed because variants are separate top-level classes, not nested classes
      val parentScopeClassName = if (variantSuffix != null) {
        ClassName.get(scope.implClassName.j.packageName(), scope.implClassName.j.simpleName() + variantSuffix)
      } else {
        scope.implClassName
      }

      val parameters: Map<Type, ChildMethod.Parameter> =
          childEdge.method.parameters.associateBy { parameter -> parameter.type }
      val dependencyMethodImpls =
          getDependencyMethodData(childEdge.child).map { methodData ->
            childDependencyMethodImpl(parameters, methodData, parentScopeClassName)
          }
      val isAbstractClass = dependencyMethodImpls.any { it.isInternal }

      return ChildDependenciesImpl(
          childEdge.child.dependenciesClassName,
          dependencyMethodImpls,
          isAbstractClass,
          env,
      )
    }

    private fun childDependencyMethodImpl(
        parameters: Map<Type, ChildMethod.Parameter>,
        methodData: DependencyMethodData,
        parentScopeClassName: ClassName,
    ): ChildDependencyMethodImpl {
      val parameter = parameters[methodData.returnType]
      val returnExpression =
          if (parameter == null) {
            ChildDependencyMethodImpl.ReturnExpression.Provider(
                parentScopeClassName,
                getProviderMethodName(methodData.returnType),
            )
          } else {
            ChildDependencyMethodImpl.ReturnExpression.Parameter(parameter.parameter.name)
          }
      val isInternal = (methodData.returnType.type as? CompilerType)?.isInternal() ?: false
      return ChildDependencyMethodImpl(
          methodData.name,
          methodData.returnTypeName,
          returnExpression,
          isInternal,
      )
    }

    private fun scopeProviderMethod(): ScopeProviderMethod {
      val name = getProviderMethodName(Type(scope.clazz.type, null))
      val isInternal = (scope.clazz.type as? CompilerType)?.isInternal() ?: false
      return ScopeProviderMethod(name, scope.typeName, isInternal)
    }

    private fun factoryProviderMethods(useSelectiveCaching: Boolean): List<FactoryProviderMethod> =
        scope.factoryMethods.map { factoryMethod ->
          val returnType = factoryMethod.returnType.type
          val spreadProviderMethods =
              factoryMethod.spread?.let { spreadProviderMethods(it) } ?: emptyList()
          FactoryProviderMethod(
              getProviderMethodName(returnType),
              returnType.type.typeName,
              factoryProviderMethodBody(factoryMethod, useSelectiveCaching),
              spreadProviderMethods,
              env,
          )
        }

    private fun factoryProviderMethodBody(factoryMethod: FactoryMethod, useSelectiveCaching: Boolean): FactoryProviderMethodBody {
      val instantiation =
          when (factoryMethod) {
            is BasicFactoryMethod -> basicInstantiation(factoryMethod)
            is ConstructorFactoryMethod -> constructorInstantiation(factoryMethod)
            is BindsFactoryMethod -> bindsInstantiation(factoryMethod)
          }

      val shouldBeCached = !shouldSkipCaching(factoryMethod, useSelectiveCaching) &&
          if (useSelectiveCaching) {
            shouldCache(factoryMethod)
          } else {
            factoryMethod.isCached
          }

      return if (shouldBeCached) {
        FactoryProviderMethodBody.Cached(
            getCacheFieldName(factoryMethod.returnType.type),
            factoryMethod.returnType.type.type.typeName,
            instantiation,
            env,
        )
      } else {
        FactoryProviderMethodBody.Uncached(instantiation)
      }
    }

    private fun spreadProviderMethods(spread: Spread): List<SpreadProviderMethod> =
        spread.methods.map { method ->
          SpreadProviderMethod(
              getProviderMethodName(method.returnType),
              method.method.isStatic(),
              method.returnType.type.typeName,
              method.sourceType.type.typeName,
              getProviderMethodName(method.sourceType),
              method.name,
          )
        }

    private fun basicInstantiation(
        factoryMethod: BasicFactoryMethod,
    ): FactoryProviderInstantiation.Basic =
        FactoryProviderInstantiation.Basic(
            OBJECTS_FIELD_NAME,
            factoryMethod.objects.clazz.typeName,
            factoryMethod.isStatic,
            factoryMethod.name,
            callProviders(factoryMethod),
        )

    private fun constructorInstantiation(
        factoryMethod: ConstructorFactoryMethod,
    ): FactoryProviderInstantiation.Constructor =
        FactoryProviderInstantiation.Constructor(
            factoryMethod.returnType.type.type.typeName,
            callProviders(factoryMethod),
        )

    private fun bindsInstantiation(
        factoryMethod: BindsFactoryMethod,
    ): FactoryProviderInstantiation.Binds =
        FactoryProviderInstantiation.Binds(
            getProviderMethodName(factoryMethod.parameters.single().type),
        )

    private fun callProviders(factoryMethod: FactoryMethod): CallProviders {
      val names =
          factoryMethod.parameters.map { parameter -> getProviderMethodName(parameter.type) }
      return CallProviders(names)
    }

    private fun dependencyProviderMethods(): List<DependencyProviderMethod> =
        getDependencyMethodData(scope).map { methodData ->
          DependencyProviderMethod(
              getProviderMethodName(methodData.returnType),
              methodData.returnTypeName,
              DEPENDENCIES_FIELD_NAME,
              methodData.name,
              env,
          )
        }

    private fun objectsImpl(): ObjectsImpl? {
      val objects = scope.objects ?: return null
      val objectsClassName = scope.objectsClassName ?: return null
      val abstractMethods =
          objects.factoryMethods
              .filter { it.method.isAbstract() }
              .map { ObjectsAbstractMethod(env, it.method as CompilerMethod) }
      return ObjectsImpl(
          scope.objectsImplClassName,
          objectsClassName,
          objects.clazz.kind == IrClass.Kind.INTERFACE,
          abstractMethods,
      )
    }

    private fun dependencies(forceGenerate: Boolean = false): Dependencies? {
      if (!forceGenerate && scope.dependencies != null) {
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
                isInternal,
            )
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
                if (callerMethod.isConstructor) {
                  ownerType.simpleName.substringBefore('<')
                } else {
                  callerMethod.name
                }
            val paramList = callerMethod.parameters.map { removeGenerics(it.type.qualifiedName) }

            JavaDocMethodLink(owner, methodName, paramList)
          }
      return DependencyMethodJavaDoc(requestedFrom)
    }

    private fun removeGenerics(name: String): String = name.takeWhile { it != '<' }

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

    // === Selective Caching Logic for SMART_CACHE ===

    /**
     * Determines whether a factory method should be cached based on usage patterns.
     * Used in SMART_CACHE mode to optimize memory usage by skipping caching for
     * single-use, internal-only dependencies.
     */
    private fun shouldCache(factoryMethod: FactoryMethod): Boolean {
      val returnType = factoryMethod.returnType.type

      // Check memoization cache first
      shouldCacheCache[returnType]?.let { return it }

      // Detect cycles (shouldn't happen with valid DI graphs, but be defensive)
      if (returnType in shouldCacheComputing) {
        // Conservative: cache dependencies involved in cycles to break the cycle
        // Unless explicitly marked with @DoNotCache for all modes (not just SmartCache)
        return !factoryMethod.hasDoNotCache || factoryMethod.doNotCacheOnlyForSmartCache
      }

      // Mark as computing to detect cycles
      shouldCacheComputing.add(returnType)

      try {
        val result = computeShouldCache(factoryMethod)
        shouldCacheCache[returnType] = result
        return result
      } finally {
        shouldCacheComputing.remove(returnType)
      }
    }

    /**
     * Core caching decision logic applying multiple heuristics.
     */
    private fun computeShouldCache(factoryMethod: FactoryMethod): Boolean {
      val returnType = factoryMethod.returnType.type

      // Rule 1: If method has @DoNotCache annotation, never cache
      if (factoryMethod.hasDoNotCache) {
        return false
      }

      // Rule 2: Skip cache if return type has @DoNotCache annotation
      if (hasDoNotCacheAnnotation(returnType)) {
        return false
      }

      // Rule 3: Skip cache for abstract passthrough methods
      // These are abstract methods that just cast/forward a parameter to a different type
      // with no construction cost
      if (isPassthroughMethod(factoryMethod)) {
        return false
      }

      // Rule 4: Check if this dependency has public accessor method
      val hasAccessor = scope.accessMethods.any { it.returnType == returnType }
      if (hasAccessor) {
        return true
      }

      // Count how many times this dependency is used internally
      val usageCount = countInternalUsage(returnType)

      // Rule 5: Dead code - not used at all, never cache
      if (usageCount == 0) {
        return false
      }

      // Rule 6: Used multiple times internally
      if (usageCount > 1) {
        return true
      }

      // Rule 7: Has @Expose annotation
      if (factoryMethod.isExposed) {
        return true
      }

      // Rule 8: Usage count = 1 and the dependent is cached or used once.
      return !isDependentCreatedOnce(returnType)
    }

    /**
     * Checks if a method is a simple passthrough (abstract single-parameter method
     * that just casts/forwards without construction overhead).
     */
    private fun isPassthroughMethod(factoryMethod: FactoryMethod): Boolean {
      // Only check abstract methods - concrete methods are kept as-is
      if (!factoryMethod.method.isAbstract()) {
        return false
      }

      // Must have exactly 1 parameter
      if (factoryMethod.parameters.size != 1) {
        return false
      }

      val paramType = factoryMethod.parameters.single().type
      val returnType = factoryMethod.returnType.type.type as? CompilerType ?: return false
      val paramCompilerType = paramType.type as? CompilerType ?: return false

      // Check if return type is assignable from parameter type
      val isAssignable = try {
        val returnXType = env.findType(returnType.qualifiedName)
        val paramXType = env.findType(paramCompilerType.qualifiedName)

        if (returnXType != null && paramXType != null) {
          returnXType.isAssignableFrom(paramXType)
        } else {
          false
        }
      } catch (e: Exception) {
        false
      }

      return isAssignable
    }

    /**
     * Checks if a type has @DoNotCache annotation (walks up the class hierarchy).
     */
    private fun hasDoNotCacheAnnotation(type: Type): Boolean {
      val compilerType = type.type as? CompilerType ?: return false

      try {
        val xType = env.findType(compilerType.qualifiedName) ?: return false
        val typeElement = xType.typeElement ?: return false

        // Walk up the class hierarchy checking for @DoNotCache annotation
        var currentElement = typeElement
        val visited = mutableSetOf<String>()

        while (true) {
          val qname = currentElement.qualifiedName

          // Prevent infinite loops
          if (qname in visited) break
          visited.add(qname)

          // Check if current class has @DoNotCache annotation
          if (currentElement.hasAnnotation(motif.DoNotCache::class)) {
            return true
          }

          // Move to superclass
          val superType = currentElement.superClass
          if (superType == null || superType.typeElement == null) {
            break
          }

          currentElement = superType.typeElement!!

          // Stop at Object/Any
          val superQName = currentElement.qualifiedName
          if (superQName == "java.lang.Object" || superQName == "kotlin.Any") {
            break
          }
        }

        return false
      } catch (e: Exception) {
        return false
      }
    }

    /**
     * Checks if the single dependent of a type is created only once.
     */
    private fun isDependentCreatedOnce(type: Type): Boolean {
      // Since this is only called when usageCount == 1, there's exactly one dependent
      val dependent = dependentsCache[type]?.singleOrNull() ?: return true

      // If the dependent has @DoNotCache, it will be recreated each time
      // So we should cache this dependency to avoid recreating it
      if (dependent.hasDoNotCache) {
        return false  // Dependent is NOT created once -> cache this dependency
      }

      // If the dependent is cached, it's only created once
      if (shouldCache(dependent)) {
        return true
      }

      // If the dependent is not cached, check how many times it's used
      val dependentUsageCount = dependentsCache[dependent.returnType.type]?.size ?: 0

      // Return true if the dependent is only used once
      return dependentUsageCount <= 1
    }

    /**
     * Determines if caching should be skipped based on @DoNotCache annotation settings.
     */
    private fun shouldSkipCaching(factoryMethod: FactoryMethod, useSelectiveCaching: Boolean): Boolean {
      if (!factoryMethod.hasDoNotCache) {
        return false
      }

      // If onlyForSmartCacheMode = true, only skip caching in SMART_CACHE mode
      if (factoryMethod.doNotCacheOnlyForSmartCache) {
        return useSelectiveCaching
      }

      // Otherwise, skip in all modes
      return true
    }

    /**
     * Returns the number of times a type is used internally within the scope.
     */
    private fun countInternalUsage(type: Type): Int {
      return dependentsCache[type]?.size ?: 0
    }
  }

  private class DependencyMethodData(
      val name: String,
      val returnTypeName: TypeName,
      val returnType: Type,
      val sinks: List<Sink>,
  )

  private fun getDependencyMethodData(scope: Scope): List<DependencyMethodData> =
      dependencyMethods.computeIfAbsent(scope) { createDependencyMethods(scope) }

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

  /**
   * Resolves the caching strategy from the @Scope annotation.
   * Defaults to BASELINE for backwards compatibility and safety.
   */
  private fun resolveCachingStrategy(scopeAnnotation: motif.ast.IrAnnotation): motif.CachingStrategy {
    val strategyValue = scopeAnnotation.annotationValueMap[SCOPE_ANNOTATION_FIELD_CACHING_STRATEGY]
        ?: return motif.CachingStrategy.BASELINE  // Default to BASELINE if not specified

    return when (strategyValue.toString()) {
      "SMART_CACHE" -> motif.CachingStrategy.SMART_CACHE
      "BASELINE_WITH_LOCK_SELECTABLE" -> motif.CachingStrategy.BASELINE_WITH_LOCK_SELECTABLE
      "RUNTIME_SELECTABLE" -> motif.CachingStrategy.RUNTIME_SELECTABLE
      "BASELINE" -> motif.CachingStrategy.BASELINE
      else -> motif.CachingStrategy.BASELINE  // Unknown strategies default to BASELINE
    }
  }

  companion object {

    private const val OBJECTS_FIELD_NAME = "objects"
    private const val DEPENDENCIES_FIELD_NAME = "dependencies"
    private const val SCOPE_ANNOTATION_FIELD_CACHING_STRATEGY = "cachingStrategy"

    fun create(env: XProcessingEnv, graph: ResolvedGraph): List<ScopeImpl> =
        ScopeImplFactory(env, graph).create()
  }
}
