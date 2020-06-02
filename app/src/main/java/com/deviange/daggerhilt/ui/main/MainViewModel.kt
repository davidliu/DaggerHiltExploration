package com.deviange.daggerhilt.ui.main

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.deviange.daggerhilt.Repository

class MainViewModel @ViewModelInject constructor(
    // Not used but to demonstrate that other objects in the graph can be injected.
    private val repository: Repository,
    @Assisted private val savedStateHandle: SavedStateHandle
): ViewModel() {

    val counterState = savedStateHandle.getLiveData("counter", 1)
}
