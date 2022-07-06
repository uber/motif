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
package motif.ast.intellij

import com.intellij.lang.jvm.JvmModifier
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.psi.JvmPsiConversionHelper
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiElementFactory
import com.intellij.psi.PsiResolveHelper
import kotlin.collections.filter
import kotlin.collections.map
import motif.ast.IrAnnotation
import motif.ast.IrClass
import motif.ast.IrField
import motif.ast.IrMethod
import motif.ast.IrModifier
import motif.ast.IrType

class IntelliJClass(
    private val project: Project,
    private val psiClassType: PsiClassType,
    val psiClass: PsiClass
) : IrUtil, IrClass {

  private val jvmPsiConversionHelper =
      ServiceManager.getService(project, JvmPsiConversionHelper::class.java)

  override val type: IrType by lazy { IntelliJType(project, psiClassType) }

  override val supertypes: List<IrType> by lazy {
    psiClassType.superTypes.map { IntelliJType(project, it) }
  }

  override val typeArguments: List<IrType> by lazy {
    psiClassType.typeArguments().map { jvmPsiConversionHelper.convertType(it) }.map {
      IntelliJType(project, it)
    }
  }

  override val kind: IrClass.Kind by lazy {
    if (psiClass.isInterface) {
      IrClass.Kind.INTERFACE
    } else {
      IrClass.Kind.CLASS
    }
  }

  override val methods: List<IrMethod> by lazy {
    val resolverHelper = PsiResolveHelper.SERVICE.getInstance(project)
    psiClass.visibleSignatures
        .filter {
          it.method.containingClass?.qualifiedName != "java.lang.Object" &&
              !it.method.hasModifier(JvmModifier.PRIVATE) &&
              !it.isConstructor &&
              resolverHelper.isAccessible(it.method, psiClass, null)
        }
        .map { IntelliJMethod(project, it.method, it.substitutor) }
  }

  override val nestedClasses: List<IrClass> by lazy {
    psiClass.allInnerClasses.map {
      val psiClassType: PsiClassType = PsiElementFactory.SERVICE.getInstance(project).createType(it)
      IntelliJClass(project, psiClassType, psiClassType.resolve()!!)
    }
  }

  override val fields: List<IrField> by lazy {
    psiClass.allFields.filter { it.containingClass?.qualifiedName != "java.lang.Object" }.map {
      IntelliJField(project, it)
    }
  }

  override val constructors: List<IrMethod> by lazy {
    val substitutor = psiClassType.resolveGenerics().substitutor
    psiClass.constructors.map { IntelliJMethod(project, it, substitutor) }
  }

  override val annotations: List<IrAnnotation> by lazy {
    try {
      psiClass.modifierList!!.irAnnotations(project)
    } catch (e: Exception) {
      throw e
    }
  }

  override val modifiers: Set<IrModifier> by lazy { psiClass.irModifiers() }
}
