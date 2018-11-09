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
package motif.intellij.validation.ir

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiElementFactory
import motif.models.java.*

// TODO Handle generics
class IntelliJClass(
        private val project: Project,
        private val psiClassType: PsiClassType) : IrUtil, IrClass {

    val psiClass: PsiClass by lazy { psiClassType.resolve()!! }

    override val type: IrType by lazy { IntelliJType(project, psiClassType) }

    override val kind: IrClass.Kind by lazy {
        if (psiClass.isInterface) {
            IrClass.Kind.INTERFACE
        } else {
            IrClass.Kind.CLASS
        }
    }

    // TODO Verify that this includes static methods for this class and does NOT include static methods from superclasses.
    override val methods: List<IrMethod> by lazy {
        psiClass.allMethods
                .filter { it.containingClass?.qualifiedName != "java.lang.Object"}
                .map { IntelliJMethod(project, it) }
    }

    override val nestedClasses: List<IrClass> by lazy {
        psiClass.allInnerClasses.map {
            val psiClassType: PsiClassType = PsiElementFactory.SERVICE.getInstance(project).createType(it)
            IntelliJClass(project, psiClassType)
        }
    }

    override val fields: List<IrField> by lazy {
        psiClass.allFields
                .filter { it.containingClass?.qualifiedName != "java.lang.Object"}
                .map { IntelliJField(project, it) }
    }

    override val constructors: List<IrMethod> by lazy {
        psiClass.constructors.map { IntelliJMethod(project, it) }
    }

    override val annotations: List<IrAnnotation> by lazy { psiClass.modifierList!!.irAnnotations(project) }

    override val modifiers: Set<IrModifier> by lazy { psiClass.irModifiers() }
}