package com.atlasstudio.naurad.ui.favourites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atlasstudio.naurad.data.LocationWithOffices
import com.atlasstudio.naurad.data.TouchedLocation
import com.atlasstudio.naurad.repository.LocationOfficeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavouritesViewModel @Inject constructor(
    private val repo : LocationOfficeRepository
) : ViewModel() {
    private val state = MutableStateFlow<FavouritesFragmentState>(FavouritesFragmentState.Init)
    val mState: StateFlow<FavouritesFragmentState> get() = state

    suspend fun createFavouritesFlow(): Flow<List<TouchedLocation>> {
        return repo.getFavouriteLocations()
    }

    fun onStart() {
        initialize()
    }

    fun onFavouriteSelected(location : TouchedLocation) {
        viewModelScope.launch {
            repo.getLocatedOfficesForFavourite(location)
                .collect {
                    navigateBack(LocationWithOffices(it.location, it.offices))
                }
        }
    }

    fun onFavouriteDelete(location: TouchedLocation) {
        viewModelScope.launch {
            repo.deleteLocation(location)
        }
        showDeletedSnackBar()
    }

    private fun initialize() {
        state.value = FavouritesFragmentState.Init
    }

    private fun navigateBack(location : LocationWithOffices) {
        state.value = FavouritesFragmentState.NavigateBackWithResult(location)
    }

    private fun showDeletedSnackBar() {
        state.value = FavouritesFragmentState.SnackBarFavouriteDeleted
    }
}

sealed class FavouritesFragmentState {
    object Init : FavouritesFragmentState()
    data class NavigateBackWithResult(val location : LocationWithOffices) : FavouritesFragmentState()
    object SnackBarFavouriteDeleted: FavouritesFragmentState()
}