package com.uber.motif.intellij.model

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.uber.motif.intellij.index.ScopeIndex

class Playground {

    companion object {

        fun run(project: Project) {
            val scopeSuperset = ScopeIndex.getScopeFileSuperset(project)
            scopeSuperset.map {
                PsiManager.getInstance(project).findFile(it)
            }.forEach {
                println(it)
            }
        }
    }
}