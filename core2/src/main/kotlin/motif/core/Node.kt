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
package motif.core

import motif.models.*

/**
 * Represents the site of a request for a dependency ([Sink]) or the site at which
 * a dependency is provided ([Source]).
 */
sealed class Node {
    abstract val scope: Scope
    abstract val type: Type
}

/**
 * The site at which a dependency is provided.
 */
sealed class Source : Node() {

    abstract val isExposed: Boolean

    // Whether or not this Source can override other sources. For instance, a ChildParameterSource is able to
    // override existing factory methods that satisfy the same Type.
    abstract val isOverriding: Boolean
}

/**
 * The site of a request for a dependency.
 */
sealed class Sink : Node()

class FactoryMethodSource(val factoryMethod: FactoryMethod) : Source() {

    override val scope = factoryMethod.objects.scope
    override val type = factoryMethod.returnType.type
    override val isExposed = factoryMethod.isExposed
    override val isOverriding = false
}

class ScopeSource(override val scope: Scope) : Source() {

    override val type = Type(scope.clazz.type, null)
    override val isExposed = false
    override val isOverriding = false
}

class SpreadSource(val spreadMethod: Spread.Method) : Source() {

    override val scope = spreadMethod.spread.factoryMethod.objects.scope
    override val type = spreadMethod.returnType
    override val isExposed = spreadMethod.spread.factoryMethod.isExposed
    override val isOverriding = false
}

class ChildParameterSource(val parameter: ChildMethod.Parameter) : Source() {

    override val scope = parameter.method.scope
    override val type = parameter.type
    override val isExposed = parameter.isExposed
    override val isOverriding = true
}

class FactoryMethodSink(val parameter: FactoryMethod.Parameter) : Sink() {

    override val scope = parameter.factoryMethod.objects.scope
    override val type = parameter.type
}

class AccessMethodSink(val accessMethod: AccessMethod) : Sink() {

    override val scope = accessMethod.scope
    override val type = accessMethod.returnType
}
