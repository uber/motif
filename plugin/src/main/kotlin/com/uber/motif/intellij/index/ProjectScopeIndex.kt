package com.uber.motif.intellij.index

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.ProjectScopeBuilder
import com.intellij.util.indexing.FileBasedIndex

class ProjectScopeIndex(private val project: Project) {

    private val index = FileBasedIndex.getInstance()

    fun refreshFile(file: VirtualFile) {
        val projectScopeBuilder = ProjectScopeBuilder.getInstance(project)
        // processValues forces the file to be reindexed
        index.processValues(
                ScopeIndex.ID,
                true,
                file,
                { _, _ -> true },
                projectScopeBuilder.buildProjectScope())
    }

    /**
     * Returns a superset of all files that contain a Motif Scope.
     */
    fun getScopeFileSuperset(): Set<VirtualFile> {
        val scopeFiles = mutableSetOf<VirtualFile>()
        val projectScopeBuilder = ProjectScopeBuilder.getInstance(project)
        index.getFilesWithKey(ScopeIndex.ID, setOf(true), {
            scopeFiles.add(it)
            true
        }, projectScopeBuilder.buildProjectScope())
        return scopeFiles
    }
}