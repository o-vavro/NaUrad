package com.atlasstudio.naurad.data

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize

@Parcelize
data class AddressedLocationWithOffices(
    val location: LatLng?,
    val address: String,
    val offices: List<Office?>
    ) : Parcelable