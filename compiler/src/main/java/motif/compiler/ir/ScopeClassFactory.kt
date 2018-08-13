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
package motif.compiler.ir

import motif.compiler.errors.parsing.ParsingError
import motif.compiler.javax.JavaxUtil
import motif.compiler.ir
import motif.ir.source.ScopeClass
import motif.ir.source.accessmethod.AccessMethod
import motif.ir.source.child.ChildMethod
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.type.DeclaredType

class ScopeClassFactory(override val env: ProcessingEnvironment) : JavaxUtil {

    private val childFactory = ChildDeclarationFactory(env)
    private val accessMethodFactory = AccessMethodFactory(env)
    private val objectsClassFactory = ObjectsImplFactory(env)
    private val explicitDependenciesFactory = ExplicitDependenciesFactory(env)

    fun create(scopeType: DeclaredType): ScopeClass {
        val childMethods: MutableList<ChildMethod> = mutableListOf()
        val accessMethods: MutableList<AccessMethod> = mutableListOf()

        scopeType.methods()
                .forEach {
                    when {
                        childFactory.isApplicable(it) -> childMethods.add(childFactory.create(it))
                        accessMethodFactory.isApplicable(it) -> accessMethods.add(accessMethodFactory.create(it))
                        else -> throw ParsingError(it.element, "Invalid Scope method")
                    }
                }

        val objectsClass = objectsClassFactory.create(scopeType)
        val explicitDependencies = explicitDependenciesFactory.create(scopeType)

        return ScopeClass(
                scopeType,
                scopeType.ir,
                childMethods,
                accessMethods,
                objectsClass,
                explicitDependencies)
    }
}