/*
 * Copyright (c) 2018 Uber Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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