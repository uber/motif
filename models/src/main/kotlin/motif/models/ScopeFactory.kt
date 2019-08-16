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

import motif.ScopeFactory
import motif.ast.IrClass

class ScopeFactory(val clazz: IrClass) {

    private val scopeFactorySuperclass: IrClass = getScopeFactorySuperclass(clazz) ?:
            throw RuntimeException("Could not find ScopeFactory superclass.")

    val scopeClass: IrClass = run {
        val scopeClass = scopeFactorySuperclass.typeArguments[0].resolveClass()
                ?: throw InvalidScopeFactoryTypeArgument(clazz, scopeFactorySuperclass.typeArguments[0])
        if (!scopeClass.hasAnnotation(motif.Scope::class)) {
            throw UnannotatedScopeFactoryScope(clazz, scopeClass)
        }
        scopeClass
    }
    val dependencies: Dependencies = run {
        val dependenciesClass = scopeFactorySuperclass.typeArguments[1].resolveClass()
                ?: throw InvalidScopeFactoryTypeArgument(clazz, scopeFactorySuperclass.typeArguments[1])
        Dependencies(dependenciesClass)
    }

    private fun getScopeFactorySuperclass(clazz: IrClass): IrClass? {
        val qualifiedName = clazz.qualifiedName
        if (qualifiedName == "java.lang.Object") return null
        if (qualifiedName.startsWith("${ScopeFactory::class.java.name}<")) return clazz
        val superclass = clazz.superclass?.resolveClass() ?: return null
        return getScopeFactorySuperclass(superclass)
    }
}