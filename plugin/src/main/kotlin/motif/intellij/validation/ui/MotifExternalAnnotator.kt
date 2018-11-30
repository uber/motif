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
package motif.intellij.validation.ui

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.psi.PsiFile

class MotifExternalAnnotator : ExternalAnnotator<GraphState, GraphState>() {

    override fun apply(file: PsiFile, state: GraphState, holder: AnnotationHolder) {
        state.errors
                .filter { error -> error.psiElement?.containingFile == file }
                .forEach { error ->
                    error.psiElement?.let { psiElement ->
                        holder.createErrorAnnotation(psiElement, error.message)
                    }
                }
    }

    override fun collectInformation(file: PsiFile): GraphState {
        return GraphStateManager.refresh(file.project)
    }

    override fun doAnnotate(collectedInfo: GraphState): GraphState {
        return collectedInfo
    }
}