package com.uber.motif.intellij

import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiTreeChangeEvent
import com.uber.motif.intellij.graph.GraphProcessor
import com.uber.motif.intellij.index.ScopeIndex
import com.uber.motif.intellij.psi.PsiTreeChangeAdapter
import com.uber.motif.intellij.psi.isMaybeScopeFile

class MotifComponent(private val project: Project) : ProjectComponent {

    val graphProcessor = GraphProcessor(project)

    private val scopeIndex = ScopeIndex.getInstance()

    override fun projectOpened() {
        PsiManager.getInstance(project).addPsiTreeChangeListener(object : PsiTreeChangeAdapter() {

            override fun onChange(event: PsiTreeChangeEvent) {
                val psiFile: PsiFile = event.file ?: return
                if (psiFile.isMaybeScopeFile()) {
                    scopeIndex.refreshFile(project, psiFile.virtualFile)
                }
            }
        })
    }

    companion object {

        fun get(project: Project): MotifComponent {
            return project.getComponent(MotifComponent::class.java)
        }
    }
}