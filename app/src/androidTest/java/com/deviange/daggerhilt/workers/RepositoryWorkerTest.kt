package com.deviange.daggerhilt.workers

import androidx.hilt.work.HiltWorkerFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import androidx.work.workDataOf
import com.deviange.daggerhilt.MainAppModule
import com.deviange.daggerhilt.Repository
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(MainAppModule::class)
class RepositoryWorkerTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @JvmField
    @BindValue
    val testRepository: Repository = TestRepository()

    @Before
    fun beforeEach() {
        hiltRule.inject()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val config = Configuration.Builder()
            .setExecutor(SynchronousExecutor())
            .setWorkerFactory(workerFactory)
            .build()

        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    }

    @Test
    fun verifyWorkerOutputsCorrectData() {
        val workManager = WorkManager.getInstance(ApplicationProvider.getApplicationContext())
        val request = OneTimeWorkRequestBuilder<RepositoryWorker>().build()
        // Customize the behavior of the repository depending on the test case.
        testRepository.counter = Int.MAX_VALUE

        workManager.enqueue(request).result.get()

        val workInfo = workManager.getWorkInfoById(request.id).get()
        val outputData = workInfo.outputData
        Assert.assertEquals(workDataOf("counter" to Int.MAX_VALUE), outputData)
    }

    class TestRepository : Repository {
        override var counter: Int = 1
    }
}
