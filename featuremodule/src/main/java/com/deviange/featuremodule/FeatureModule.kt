package com.deviange.featuremodule

import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent

@Module
@InstallIn(ApplicationComponent::class)
object FeatureModule {
    @Provides
    fun feature(): Feature = RealFeature()
}

interface Feature {
    fun doThing()
}

class RealFeature : Feature {
    override fun doThing() {
        Log.v("Feature", "Doing the thing!")
    }
}
