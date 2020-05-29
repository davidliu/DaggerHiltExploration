package com.deviange.daggerhilt

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ApplicationComponent

@HiltAndroidApp
class MainApplication : Application()

@EntryPoint
@InstallIn(ActivityComponent::class)
interface RepositoryLocator {
    fun getRepository(): Repository
}

@Module
@InstallIn(ApplicationComponent::class)
object MainAppModule {
    @Provides
    fun repository() = Repository()
}