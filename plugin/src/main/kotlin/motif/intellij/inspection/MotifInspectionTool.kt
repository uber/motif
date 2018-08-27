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
package motif.intellij.inspection

import com.intellij.codeInspection.*
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiMethod
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import motif.intellij.inspection.error.ErrorHandler
import motif.intellij.psi.containedByScopeClass
import motif.ir.SourceSetGenerator
import motif.models.graph.Graph
import motif.models.graph.GraphFactory
import motif.models.graph.errors.GraphError
import motif.models.parsing.errors.ParsingError

class MotifInspectionTool: AbstractBaseJavaLocalInspectionTool() {

    private var printedMessages: MutableSet<String> = mutableSetOf()

    override fun checkMethod(method: PsiMethod, manager: InspectionManager, isOnTheFly: Boolean):
    Array<ProblemDescriptor>? {
        if (DumbService.isDumb(manager.project)) return null
        if(!method.containingClass!!.containedByScopeClass()) return null
        try {
            // this needs to be optimized, this code is super bad.
            val scopes = SourceSetGenerator().createSourceSet(manager.project)
            val processor: Graph = GraphFactory.create(scopes)
            val errors = processor.validationErrors
            if (errors.isNotEmpty()) {
                errors
                        .map { getValidationResult(it, method).errorMessage }
                        .filter { it !in printedMessages }
                        .forEachIndexed { idx, it ->
                            if (idx == 0) println("============================\nMotif Graph Validation Errors\n============================")
                            printedMessages.add(it)
                            println(it)
                        }
                val specificErrors = errors.filter { hasValidationErrors(it, method) }
                return if (specificErrors.isNotEmpty()) {
                    specificErrors
                            .map {
                                val validationResult = getValidationResult(it, method)
                                manager.createProblemDescriptor(
                                        method,
                                        validationResult.errorMessage,
                                        validationResult.fixes,
                                        ProblemHighlightType.ERROR,
                                        isOnTheFly,
                                        false
                                )
                            }.toTypedArray()
                } else null
            }
        } catch (e: ParsingError) {
            // add check to see if this is the method on which the parsing error occurred.
            println("Parsing Error " + e.message)
            return arrayOf(manager.createProblemDescriptor(method, e::class.java.name, ParsingErrorQuickFix(e),
                    ProblemHighlightType.ERROR, isOnTheFly))
        }
        return null
    }

    override fun getDisplayName(): String {
        return "Motif Dependency Inspections"
    }

    override fun getGroupDisplayName(): String {
        return "Motif Inspections"
    }

    private fun getValidationResult(error: GraphError, method: PsiMethod): ErrorHandler.ValidationResult {
        return ErrorHandler.handle(error, method)
    }

    private fun hasValidationErrors(error: GraphError, method: PsiMethod): Boolean {
        return ErrorHandler.isApplicable(error, method)
    }

    private class ParsingErrorQuickFix(private val error: ParsingError) : LocalQuickFix {

        override fun getFamilyName(): String {
            return error::class.java.name
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            println("Apply Parsing Fix invoked")
        }
    }
}
