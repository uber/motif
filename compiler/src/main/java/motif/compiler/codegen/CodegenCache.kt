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
package motif.compiler.codegen

import com.google.auto.common.MoreElements
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import motif.compiler.GENERATED_DEPENDENCIES_NAME
import motif.ir.graph.Scope
import motif.ir.source.base.Dependency
import motif.ir.source.objects.ObjectsClass
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Modifier
import javax.lang.model.type.DeclaredType
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

open class CodegenCache(
        override val env: ProcessingEnvironment,
        val cacheScope: CacheScope) : JavaPoetUtil {

    val Scope.daggerComponentName: ClassName by cache {
        val simpleName = componentTypeName.simpleNames().joinToString("_")
        implTypeName.peerClass("Dagger$simpleName")
    }

    val Scope.moduleTypeName: ClassName by cache {
        implTypeName.nestedClass("Module")
    }

    val Scope.objectsImplTypeName: ClassName by cache {
        implTypeName.nestedClass("Objects")
    }

    val ObjectsClass.typeName: ClassName by cache {
        ClassName.get(type) as ClassName
    }

    val Scope.implTypeName: ClassName by cache {
        scopeImpl(type)
    }

    val Scope.componentTypeName: ClassName by cache {
        implTypeName.nestedClass("Component")
    }

    val Scope.dependenciesTypeName: ClassName by cache {
        implTypeName.nestedClass(GENERATED_DEPENDENCIES_NAME)
    }

    val Scope.packageName: String by cache {
        MoreElements.getPackage(type.asElement()).qualifiedName.toString()
    }

    val Scope.typeName: ClassName by cache {
        ClassName.get(scopeClass.userData as DeclaredType) as ClassName
    }

    val Scope.componentMethodSpecs: Map<Dependency, MethodSpec> by cache {
        val accessMethodDependencies = accessMethods.map { it.dependency }
        childRequiredDependencies.abstractMethodSpecs(accessMethodDependencies)
    }

    val Scope.componentFieldSpec: FieldSpec by cache {
        FieldSpec.builder(componentTypeName, "component", Modifier.PRIVATE, Modifier.FINAL)
                .build()
    }

    private fun <R, T> cache(block: R.() -> T): ReadOnlyProperty<R, T> {
        return cacheScope.cache(block)
    }

    class CacheScope {

        private val map: MutableMap<LazyExtGet, Any?> = mutableMapOf()

        fun <R, T> cache(block: R.() -> T): ReadOnlyProperty<R, T> {
            return object: ReadOnlyProperty<R, T> {

                @Synchronized
                override fun getValue(thisRef: R, property: KProperty<*>): T {
                    val get = LazyExtGet(thisRef, property)
                    @Suppress("UNCHECKED_CAST")
                    return map.computeIfAbsent(get) { thisRef.block() } as T
                }
            }
        }

        private data class LazyExtGet(
                private val thisRef: Any?,
                private val property: KProperty<*>)
    }
}