package motif.intellij.thread

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import java.util.concurrent.*

/**
 * Single-threaded scheduler that executes the given Job when the following is true:
 *
 * 1) Indexes are available.
 * 2) Read access is allowed.
 *
 * If a ProcessedCancelledException is thrown during execution, the Job is automatically rescheduled.
 *
 * Note: It's possible that indexes are not available if runWithRetry is executed when we already have read access (See
 * DumbService.runReadActionInSmartMode for details). In this case, an IndexNotReadyException is thrown.
 */
class RetryScheduler(project: Project) {

    private val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private val dumbService: DumbService = DumbService.getInstance(project)
    private val progressManager: ProgressManager = ProgressManager.getInstance()

    private val futures: ConcurrentLinkedQueue<Future<*>> = ConcurrentLinkedQueue()

    fun runWithRetry(job: Job) {
        val future = scheduler.submit {
            dumbService.runReadActionInSmartMode {
                val success = progressManager.runInReadActionWithWriteActionPriority({
                    job.run()
                }, null)
                if (!success) {
                    job.onRetry()
                    runWithRetry(job)
                }
            }
        }
        futures.add(future)
    }

    fun waitForCompletion() {
        while (futures.isNotEmpty()) {
            futures.poll()?.get()
        }
    }

    interface Job {
        fun run()
        fun onRetry()
    }
}