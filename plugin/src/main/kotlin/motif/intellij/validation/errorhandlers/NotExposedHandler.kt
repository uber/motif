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
import motif.models.errors.NotExposedError

class NotExposedHandler: ErrorHandler<NotExposedError>() {

    override fun isApplicable(error: NotExposedError, method: PsiMethod): Boolean {
        return false
        // TOOD
        // return psiMethodEquivalence(error.factoryMethod.userData, method)
    }

    override fun message(error: NotExposedError): String {
        return "Dependency " +
                error.requiredDependency.dependency.type.simpleName +
                " is not exposed by " +
                error.scopeClass.ir.type.simpleName +
                " but is required by " +
                error.requiredDependency.consumingScopes.map { it.simpleName }
    }

    override fun fixes(error: NotExposedError): Array<LocalQuickFix> {
        return arrayOf(NotExposedFix())
    }

    class NotExposedFix: LocalQuickFix {
        override fun getFamilyName(): String {
            return "Not Exposed Error"
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            println("Apply Fix invoked on Not Exposed Error")
        }
    }
}
