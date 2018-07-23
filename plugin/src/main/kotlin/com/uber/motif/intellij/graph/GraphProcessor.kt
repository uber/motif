package com.uber.motif.intellij.graph

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.Relay
import com.uber.motif.intellij.index.ScopeIndex
import com.uber.motif.intellij.psi.getScopeClasses
import com.uber.motif.intellij.thread.RetryScheduler
import io.reactivex.Observable
import java.util.concurrent.atomic.AtomicBoolean

class GraphProcessor(private val project: Project) {

    private val psiManager = PsiManager.getInstance(project)
    private val index = ScopeIndex.getInstance()
    private val retryScheduler = RetryScheduler(project)

    private var updates: Relay<Update> = BehaviorRelay.createDefault(Update.pending()).toSerialized()

    private val isDirty = AtomicBoolean()

    fun start() {
        index.registerListener {
            updates.accept(Update.pending())
            isDirty.set(true)
            retryScheduler.runWithRetry(object: RetryScheduler.Job {
                override fun run() {
                    // It's possible that multiple refreshes have been queued up by this point. Only do work if necessary.
                    if (isDirty.compareAndSet(true, false)) {
                        val scopeClasses = getScopeClasses()
                        updates.accept(Update.update(scopeClasses))
                    }
                }

                override fun onRetry() {
                    // Job didn't actually complete so reset the dirty flag.
                    isDirty.set(true)
                }
            })
        }
    }

    fun scopeClasses(): List<PsiClass> {
        return updates.filter { !it.pending }.blockingFirst().scopeClasses
    }

    private fun getScopeClasses(): List<PsiClass> {
        return index.getSnapshot(project).files
                .mapNotNull { psiManager.findFile(it) }
                .flatMap { it.getScopeClasses() }
    }

    data class Update(val scopeClasses: List<PsiClass>, val pending: Boolean) {

        companion object {

            fun pending(): Update {
                return Update(listOf(), pending = true)
            }

            fun update(scopeClasses: List<PsiClass>): Update {
                return Update(scopeClasses, pending = false)
            }
        }
    }
}