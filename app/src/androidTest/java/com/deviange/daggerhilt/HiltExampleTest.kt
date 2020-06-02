package com.deviange.daggerhilt

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.deviange.featuremodule.Feature
import com.deviange.featuremodule.FeatureModule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Assert
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

    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    private val testFeature = TestFeature()

    @BindValue
    @JvmField
    val feature: Feature = testFeature

    @Test
    fun verifySomethingIsDone() {
        Assert.assertEquals(testFeature.invocationCount, 1)
    }

    @Test
    fun verifyCounterIsDisplayedCorrectly_AcrossRecreation() {
        onView(withText("MainFragment Counter: 1")).check(matches(isDisplayed()))

        activityScenarioRule.scenario.recreate()

        onView(withText("MainFragment Counter: 2")).check(matches(isDisplayed()))
    }

    class TestFeature : Feature {

        var invocationCount = 0

        override fun doThing() {
            invocationCount++
        }
    }
}
