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
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import motif.intellij.psi.getClass
import motif.models.graph.errors.*
import java.util.*

abstract class ErrorHandler<T : GraphError> {

    abstract fun message(error: T): String
    abstract fun isApplicable(error: T, method: PsiMethod): Boolean
    abstract fun fixes(error: T): Array<LocalQuickFix>

    fun error(error: T, element: PsiElement): ValidationResult {
        return ValidationResult(element, message(error), getFixes(error))
    }

    data class ValidationResult(val element: PsiElement, val errorMessage: String, val fixes: Array<LocalQuickFix>) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ValidationResult

            if (element != other.element) return false
            if (errorMessage != other.errorMessage) return false
            if (!Arrays.equals(fixes, other.fixes)) return false
            return true
        }

        override fun hashCode(): Int {
            var result = element.hashCode()
            result = 31 * result + errorMessage.hashCode()
            result = 31 * result + Arrays.hashCode(fixes)
            return result
        }
    }

    fun psiMethodEquivalence(userData: Any?, method: PsiMethod): Boolean {
        // maybe add a check to see if the containing class is the one with the error.. unless the PsiMethod
        // equivalence is already taking care of that.
        return when (userData) {
            is PsiClass -> userData == method.returnType?.getClass()
            is PsiMethod -> userData == method
            else -> false
        }
    }

    companion object {

        private val missingDependenciesHandler = MissingDependenciesHandler()
        private val scopeCycleHandler = ScopeCycleHandler()
        private val dependencyCycleHandler = DependencyCycleHandler()
        private val duplicateFactoryMethodsHandler = DuplicateFactoryMethodsHandler()
        private val notExposedHandler = NotExposedHandler()

        fun handle(error: GraphError, element: PsiElement): ValidationResult {
            return when (error) {
                is MissingDependenciesError -> missingDependenciesHandler.error(error, element)
                is ScopeCycleError -> scopeCycleHandler.error(error, element)
                is DependencyCycleError -> dependencyCycleHandler.error(error, element)
                is DuplicateFactoryMethodsError -> duplicateFactoryMethodsHandler.error(error, element)
                is NotExposedError -> notExposedHandler.error(error, element)
            }
        }

        fun isApplicable(error: GraphError, method: PsiMethod): Boolean {
            return when (error) {
                is MissingDependenciesError -> missingDependenciesHandler.isApplicable(error, method)
                is ScopeCycleError -> scopeCycleHandler.isApplicable(error, method)
                is DependencyCycleError -> dependencyCycleHandler.isApplicable(error, method)
                is DuplicateFactoryMethodsError -> duplicateFactoryMethodsHandler.isApplicable(error, method)
                is NotExposedError -> notExposedHandler.isApplicable(error, method)
            }
        }

        private fun getFixes(error: GraphError): Array<LocalQuickFix> {
            return when (error) {
                is MissingDependenciesError -> missingDependenciesHandler.fixes(error)
                is ScopeCycleError -> scopeCycleHandler.fixes(error)
                is DependencyCycleError -> dependencyCycleHandler.fixes(error)
                is DuplicateFactoryMethodsError -> duplicateFactoryMethodsHandler.fixes(error)
                is NotExposedError -> notExposedHandler.fixes(error)
                else -> throw RuntimeException("Unhandled Motif Validation Error")
            }
        }
    }
}
