package com.deviange.daggerhilt.workers

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.deviange.daggerhilt.Repository

class RepositoryWorker @WorkerInject constructor(
    private val repository: Repository,
    @Assisted private val context: Context,
    @Assisted private val workerParameters: WorkerParameters
) : Worker(context, workerParameters) {

    override fun doWork(): Result = Result.success(workDataOf("counter" to repository.counter))
}
