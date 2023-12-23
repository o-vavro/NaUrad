package com.atlasstudio.naurad.ui.maps

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atlasstudio.naurad.data.LocationWithOffices
import com.atlasstudio.naurad.data.TouchedLocation
import com.atlasstudio.naurad.net.utils.ErrorResponseType
import com.atlasstudio.naurad.repository.LocationOfficeRepository
import com.atlasstudio.naurad.utils.BaseResult
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapsViewModel @Inject constructor(
    private val repo : LocationOfficeRepository,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {
    private val state = MutableStateFlow<MapsFragmentState>(MapsFragmentState.Init)
    val mState: StateFlow<MapsFragmentState> get() = state

    private var mLastLocation: LocationWithOffices = LocationWithOffices(
        TouchedLocation(
            LatLng(49.230505, 17.657103),
            ""
        ),
        emptyList()
    )
    val lastPosition: LatLng get() = mLastLocation.location.location
    /*private var mLastZoom: Float = 14.5f
    val lastZoom: Float get() = mLastZoom*/

    fun onStart() {
        initialize()
    }

    fun onReady() {
        reMark()
    }

    fun onPositionSelected(pos: LatLng) {
        mLastLocation = LocationWithOffices(TouchedLocation(pos, ""), emptyList())
        pos?.let {
            viewModelScope.launch {
                repo.getLocatedOfficesForLocation(pos)
                    .onStart {
                        setLoading()
                    }
                    .catch { exception ->
                        hideLoading()
                        showToast(exception.message.toString())
                    }
                    .collect { result ->
                        hideLoading()
                        when(result) {
                            is BaseResult.Success -> {
                                setMarkers(result.data)
                                mLastLocation = result.data
                                checkCurrentLocationFavourite()
                            }
                            is BaseResult.Error -> {
                                showTypeToast(result.rawResponse)
                            }
                        }
                    }
            }
        }
    }

    fun onShowFavourites() {
        navigateToFavourites()
    }

    fun storeCurrentLocation() {
        viewModelScope.launch {
            repo.storeLocation(mLastLocation)
        }
    }

    fun deleteCurrentLocation() {
        viewModelScope.launch {
            repo.deleteLocation(mLastLocation)
        }
    }

    fun checkCurrentLocationFavourite() {
        viewModelScope.launch {
            repo.isLocationStored(mLastLocation.location.location)
                .collect {
                    setFavourite(it)
                }
        }
    }

    fun onFavouritesResult(location: LocationWithOffices?) {
        location?.let {
            viewModelScope.launch {
                setMarkers(location)
                mLastLocation = location
                checkCurrentLocationFavourite()
            }
        }
    }

    /*fun onCameraZoomChanged(cameraZoom: Float) {
        mLastZoom = cameraZoom
    }*/

    private fun initialize() {
        state.value = MapsFragmentState.Init
    }

    private fun reMark() {
        mLastLocation.location?.let {
            state.value = MapsFragmentState.SetMarkers(mLastLocation)
        }
    }

    private fun setLoading(){
        state.value = MapsFragmentState.IsLoading(true)
    }

    private fun hideLoading(){
        state.value = MapsFragmentState.IsLoading(false)
    }

    private fun showToast(message: String){
        state.value = MapsFragmentState.ShowToast(message)
    }

    private fun showTypeToast(error: ErrorResponseType){
        state.value = MapsFragmentState.ShowTypeToast(error)
    }

    private fun setMarkers(location: LocationWithOffices) {
        state.value = MapsFragmentState.SetMarkers(location)
    }

    private fun setFavourite(favourite: Boolean) {
        state.value = MapsFragmentState.IsFavourite(favourite)
    }

    private fun navigateToFavourites() {
        state.value = MapsFragmentState.NavigateToFavourites
    }
}

sealed class MapsFragmentState {
    object Init : MapsFragmentState()
    data class IsLoading(val isLoading : Boolean) : MapsFragmentState()
    data class ShowToast(val message : String) : MapsFragmentState()
    data class ShowTypeToast(val error : ErrorResponseType) : MapsFragmentState()
    data class SetMarkers(val location : LocationWithOffices) : MapsFragmentState()
    data class IsFavourite(val isFavourite : Boolean) : MapsFragmentState()
    object NavigateToFavourites : MapsFragmentState()
}
