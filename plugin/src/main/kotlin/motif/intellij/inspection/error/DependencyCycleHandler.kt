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
package motif.intellij.inspection.error

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiMethod
import motif.models.graph.errors.DependencyCycleError

class DependencyCycleHandler: ErrorHandler<DependencyCycleError>() {

    override fun isApplicable(error: DependencyCycleError, method: PsiMethod): Boolean {
        return false
        // TODO
        // return psiMethodEquivalence(error.scopeClass.userData, method)
    }

    override fun message(error: DependencyCycleError): String {
        return error.scopeClass.ir.type.simpleName +
                " contains the following dependency cycles: " +
                (error.cycle + error.cycle.first())
                        .mapIndexed { i, factoryMethod ->
                            if (i == 0) factoryMethod.providedDependency.type.simpleName
                            else "-> " + factoryMethod.providedDependency.type.simpleName
                        }
    }

    override fun fixes(error: DependencyCycleError): Array<LocalQuickFix> {
        return arrayOf(DependencyCycleFix())
    }

    class DependencyCycleFix: LocalQuickFix {
        override fun getFamilyName(): String {
            return "Dependency Cycle Error"
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            println("Dependency Cycle Error Fix Invoked")
        }
    }
}
