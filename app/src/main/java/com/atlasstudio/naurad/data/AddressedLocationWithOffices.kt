package com.atlasstudio.naurad.data

import com.google.android.gms.maps.model.LatLng

data class AddressedLocationWithOffices(
    val location: LatLng?,
    val address: String,
    val offices: List<Office?>
    )