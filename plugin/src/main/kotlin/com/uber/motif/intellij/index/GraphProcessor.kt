package com.uber.motif.intellij.index

import com.intellij.openapi.project.Project
import com.uber.motif.intellij.thread.RetryScheduler
import java.util.concurrent.atomic.AtomicBoolean

class GraphProcessor(private val project: Project) {

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

    private fun refresh(context: RetryScheduler.Context) {
        val snapshot: ScopeIndexSnapshot = index.getSnapshot(project)
        println(snapshot)
    }
}