package com.deviange.daggerhilt

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder().setWorkerFactory(workerFactory).build()
}

@EntryPoint
@InstallIn(ActivityComponent::class)
interface RepositoryLocator {
    fun getRepository(): Repository
}

@Module
@InstallIn(ApplicationComponent::class)
object MainAppModule {
    @Provides
    fun repository(): Repository = RealRepository()
}
