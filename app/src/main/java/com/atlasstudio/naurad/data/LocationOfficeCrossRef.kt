package com.atlasstudio.naurad.data

import androidx.room.Entity
import com.google.android.gms.maps.model.LatLng

@Entity(primaryKeys = ["locationId", "officeId"], tableName = "location_office_cross_ref")
data class LocationOfficeCrossRef(
    val locationId: LatLng,
    val officeId: String
)