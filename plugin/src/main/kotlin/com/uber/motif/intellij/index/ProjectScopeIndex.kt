package com.uber.motif.intellij.index

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.uber.motif.intellij.thread.RetryScheduler
import java.util.concurrent.atomic.AtomicBoolean

class ProjectScopeIndex(private val project: Project) {

    private val index = ScopeIndex.getInstance()
    private val retryScheduler = RetryScheduler(project)
    private val isDirty = AtomicBoolean()

    fun start() {
        index.registerListener {
            isDirty.set(true)
            retryScheduler.runWithRetry {
                // It's possible that multiple refreshes have been queued up by this point. Only do work if necessary.
                if (isDirty.compareAndSet(true, false)) {
                    refresh(this)
                }
            }
        }
    }

    fun refreshFile(file: VirtualFile) {
        index.refreshFile(project, file)
    }

    private fun refresh(context: RetryScheduler.Context) {
        index.processScopeFileSuperset(project) {
            println(it)
        }
    }
}