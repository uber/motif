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
import motif.models.errors.ScopeCycleError

class ScopeCycleHandler: ErrorHandler<ScopeCycleError>() {

    override fun isApplicable(error: ScopeCycleError, method: PsiMethod): Boolean {
        return false
        // TODO
        // return error.cycle.find { psiMethodEquivalence(it.userData, method) } != null
    }

    override fun message(error: ScopeCycleError): String {
        return "This introduces a cyclic Scope dependency: " +
                (error.cycle + error.cycle.first())
                        .mapIndexed { i, it ->
                            if (i == 0) it.simpleName
                            else "-> " + it.simpleName
                        }.joinToString { it }
    }

    override fun fixes(error: ScopeCycleError): Array<LocalQuickFix> {
        return arrayOf(ScopeCycleFix())
    }

    class ScopeCycleFix: LocalQuickFix {
        override fun getFamilyName(): String {
            return "Delete Scope Declaration"
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            descriptor.psiElement.delete()
        }
    }
}
