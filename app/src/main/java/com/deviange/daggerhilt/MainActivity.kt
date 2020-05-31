package com.deviange.daggerhilt

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.deviange.daggerhilt.ui.main.MainFragment
import com.deviange.featuremodule.Feature
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var repository: Repository

    @Inject
    lateinit var feature: Feature

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        EntryPointAccessors.fromActivity(this, RepositoryLocator::class.java).getRepository()
        feature.doThing()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }
    }
}
