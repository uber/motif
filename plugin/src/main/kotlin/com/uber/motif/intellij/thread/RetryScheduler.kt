package com.uber.motif.intellij.thread

import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.openapi.project.Project
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

/**
 * Single-threaded scheduler that executes the given work when the following is true:
 *
 * 1) Indexes are available.
 * 2) Read access is allowed.
 *
 * If a ProcessedCancelledException is thrown during execution, the work is automatically rescheduled.
 *
 * Note: It's possible that indexes are not available if runWithRetry is executed when we already have read access (See
 * DumbService.runReadActionInSmartMode for details). In this case, we catch the IndexNotReadyException internally and
 * reschedule the work like we do for ProcessedCancelledExceptions.
 */
class RetryScheduler(project: Project) {

    private val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private val dumbService: DumbService = DumbService.getInstance(project)
    private val progressManager: ProgressManager = ProgressManager.getInstance()
    private val context: Context = Context()

    fun runWithRetry(run: Context.() -> Unit) {
        scheduler.submit(object: Runnable {

            override fun run() {
                dumbService.runReadActionInSmartMode {
                    val success = progressManager.runInReadActionWithWriteActionPriority({
                        try {
                            context.run()
                        } catch (ignore : IndexNotReadyException) {
                            throw ProcessCanceledException()
                        }
                    }, null)
                    if (!success) {
                        scheduler.submit(this)
                    }
                }
            }
        })
    }

    inner class Context {

        fun checkCancelled() {
            progressManager.progressIndicator?.checkCanceled()
        }
    }
}