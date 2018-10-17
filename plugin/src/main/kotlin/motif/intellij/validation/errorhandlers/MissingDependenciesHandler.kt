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
package motif.intellij.validation.errorhandlers

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiMethod
import motif.models.errors.MissingDependenciesError

class MissingDependenciesHandler: ErrorHandler<MissingDependenciesError>() {

    override fun isApplicable(error: MissingDependenciesError, method: PsiMethod): Boolean {
        return false
        // TODO
        // return psiMethodEquivalence(error.requiredBy.scopeClass.ir.type.userData, method)
    }

    override fun message(error: MissingDependenciesError): String {
        return error.requiredBy.scopeClass.ir.type.simpleName +
                " is missing dependencies " +
                error.dependencies.map{ it.type.simpleName }
    }

    override fun fixes(error: MissingDependenciesError): Array<LocalQuickFix> {
        return arrayOf(MissingDependenciesFix())
    }

    class MissingDependenciesFix: LocalQuickFix {
        override fun getFamilyName(): String {
            return "Missing Dependencies Error"
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            println("Apply Fix invoked on Missing Dependencies Error")
        }
    }
}
