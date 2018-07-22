package com.uber.motif.intellij.graph

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiManager
import com.uber.motif.intellij.index.ScopeIndex
import com.uber.motif.intellij.psi.getScopeClasses
import com.uber.motif.intellij.thread.RetryScheduler
import java.util.concurrent.atomic.AtomicBoolean

class GraphProcessor(private val project: Project) {

    private val psiManager = PsiManager.getInstance(project)
    private val index = ScopeIndex.getInstance()
    private val retryScheduler = RetryScheduler(project)

    private val isDirty = AtomicBoolean()

    fun start() {
        index.registerListener {
            isDirty.set(true)
            retryScheduler.runWithRetry(object: RetryScheduler.Job {
                override fun run() {
                    // It's possible that multiple refreshes have been queued up by this point. Only do work if necessary.
                    if (isDirty.compareAndSet(true, false)) {
                        refresh()
                    }
                }

                override fun onRetry() {
                    // Job didn't actually complete so reset the dirty flag.
                    isDirty.set(true)
                }
            })
        }
    }

    private fun refresh() {
        val scopeClasses: List<PsiClass> = getScopeClasses()
        println(scopeClasses)
    }

    private fun getScopeClasses(): List<PsiClass> {
        return index.getSnapshot(project).files
                .mapNotNull { psiManager.findFile(it) }
                .flatMap { it.getScopeClasses() }
    }
}