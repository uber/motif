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
package motif.core

import motif.models.*

sealed class Node {

    val testString: String by lazy {
        "${this::class.java.simpleName}[$_testString]"
    }

    abstract val scope: Scope
    abstract val type: Type

    protected abstract val _testString: String
}

sealed class Source : Node() {

    abstract val isExposed: Boolean
}

sealed class Sink : Node()

class FactoryMethodSource(val factoryMethod: FactoryMethod) : Source() {

    override val scope = factoryMethod.objects.scope
    override val type = factoryMethod.returnType.type
    override val isExposed = factoryMethod.isExposed

    override val _testString: String by lazy {
        "${scope.clazz.simpleName}|${factoryMethod.name}"
    }
}

class ScopeSource(override val scope: Scope) : Source() {

    override val type = Type(scope.clazz.type, null)
    override val isExposed = false

    override val _testString: String by lazy {
        scope.clazz.simpleName
    }
}

class SpreadSource(val spreadMethod: Spread.Method) : Source() {

    override val scope = spreadMethod.spread.factoryMethod.objects.scope
    override val type = spreadMethod.returnType
    override val isExposed = spreadMethod.spread.factoryMethod.isExposed

    override val _testString: String by lazy {
        "${scope.clazz.simpleName}|${spreadMethod.returnType.type.simpleName}.${spreadMethod.name}"
    }
}

class ChildParameterSource(val parameter: ChildMethod.Parameter) : Source() {

    override val scope = parameter.method.scope
    override val type = parameter.type
    override val isExposed = parameter.isExposed

    override val _testString: String by lazy {
        "${scope.clazz.simpleName}.${parameter.method.method.name}(${parameter.parameter.name})"
    }
}

class FactoryMethodSink(val parameter: FactoryMethod.Parameter) : Sink() {

    override val scope = parameter.factoryMethod.objects.scope
    override val type = parameter.type

    override val _testString: String by lazy {
        val methodName = if (parameter.method.name == "<init>") {
            parameter.owner.simpleName
        } else {
            parameter.method.name
        }
        "${scope.clazz.simpleName}|$methodName(${parameter.parameter.name})"
    }
}

class AccessMethodSink(val accessMethod: AccessMethod) : Sink() {

    override val scope = accessMethod.scope
    override val type = accessMethod.returnType

    override val _testString: String by lazy {
        "${scope.clazz.simpleName}.${accessMethod.method.name}"
    }
}