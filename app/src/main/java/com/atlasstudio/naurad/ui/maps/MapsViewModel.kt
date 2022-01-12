package com.atlasstudio.naurad.ui.maps

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atlasstudio.naurad.data.AddressedLocationWithOffices
import com.atlasstudio.naurad.data.Office
import com.atlasstudio.naurad.net.utils.ErrorResponseType
import com.atlasstudio.naurad.repository.LocationOfficeRepository
import com.atlasstudio.naurad.utils.BaseResult
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapsViewModel @Inject constructor(
    private val repo : LocationOfficeRepository,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {
    private val state = MutableStateFlow<MapsFragmentState>(MapsFragmentState.Init)
    val mState: StateFlow<MapsFragmentState> get() = state

    private var mLastLocation: AddressedLocationWithOffices = AddressedLocationWithOffices(null, "", emptyList())

    fun lastPosition() : LatLng? {
        return mLastLocation.location
    }

    fun onPositionSelected(pos: LatLng?) {
        mLastLocation = AddressedLocationWithOffices(pos, "", emptyList())
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
                                setMarkers(result.data.offices)
                                mLastLocation = result.data
                            }
                            is BaseResult.Error -> {
                                showTypeToast(result.rawResponse)
                            }
                        }
                    }
            }
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

    private fun setMarkers(markers: List<Office?>) {
        state.value = MapsFragmentState.SetMarkers(markers)
    }
}

sealed class MapsFragmentState {
    object Init : MapsFragmentState()
    data class IsLoading(val isLoading: Boolean) : MapsFragmentState()
    data class ShowToast(val message : String) : MapsFragmentState()
    data class ShowTypeToast(val error : ErrorResponseType) : MapsFragmentState()
    data class SetMarkers(val markers : List<Office?>) : MapsFragmentState()
}
