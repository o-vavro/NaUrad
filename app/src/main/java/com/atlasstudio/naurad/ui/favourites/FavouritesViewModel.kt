package com.atlasstudio.naurad.ui.favourites

import androidx.lifecycle.ViewModel
import com.atlasstudio.naurad.data.AddressedLocationWithOffices
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class FavouritesViewModel @Inject constructor() : ViewModel() {
    private val state = MutableStateFlow<FavouritesFragmentState>(FavouritesFragmentState.Init)
    val mState: StateFlow<FavouritesFragmentState> get() = state

    fun onStart() {
        initialize()
    }

    fun onFavouriteSelected(location : AddressedLocationWithOffices) {
        navigateBack(location)
    }

    private fun initialize() {
        state.value = FavouritesFragmentState.Init
    }

    private fun navigateBack(location : AddressedLocationWithOffices) {
        state.value = FavouritesFragmentState.NavigateBackWithResult(location)
    }

}

sealed class FavouritesFragmentState {
    object Init : FavouritesFragmentState()
    data class NavigateBackWithResult(val location : AddressedLocationWithOffices) : FavouritesFragmentState()
}