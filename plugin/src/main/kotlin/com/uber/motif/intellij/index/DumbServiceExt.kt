package com.uber.motif.intellij.index

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project

object DumbServiceExt {

    fun assertNotDumb(project: Project) {
        if (DumbService.isDumb(project)) throw IllegalStateException("Project has not finished indexing.")
    }
}