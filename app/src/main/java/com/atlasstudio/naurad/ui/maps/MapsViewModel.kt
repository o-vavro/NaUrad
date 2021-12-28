package com.atlasstudio.naurad.ui.maps

import androidx.lifecycle.ViewModel
import com.atlasstudio.naurad.repository.OfficeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MapsViewModel @Inject constructor(
    private val repo : OfficeRepository
): ViewModel() {

}