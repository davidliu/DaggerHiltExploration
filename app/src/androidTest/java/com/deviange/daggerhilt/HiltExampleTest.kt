package com.deviange.daggerhilt

import androidx.test.core.app.ActivityScenario.launch
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.deviange.featuremodule.Feature
import com.deviange.featuremodule.FeatureModule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, demonstrates how you can run an instrumentation test by selectively replacing
 * bindings in the graph.
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@UninstallModules(FeatureModule::class)
class HiltExampleTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private val testFeature = TestFeature()

    @BindValue
    @JvmField
    val feature: Feature = testFeature

    @Test
    fun verifySomethingIsDone() {
        launch(MainActivity::class.java)
        assert(testFeature.invocationCount == 1)
    }

    class TestFeature : Feature {

        var invocationCount = 0

        override fun doThing() {
            invocationCount++
        }
    }
}
