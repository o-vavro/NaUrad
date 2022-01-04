package com.atlasstudio.naurad.net.model

import com.google.gson.annotations.SerializedName

data class RuianAddressResponse(
    val address: Address? = null,
    val location: Location? = null
) {
    data class Address(
        @SerializedName("Address")
        val address: String? = null
    )

    data class Location(
        val x: Double? = null,
        val y: Double? = null,
        val spatialReference: SpatialReference? = null
    ) {
        data class SpatialReference(
            val wkid: Int = 0,
            val latestWkid: Int? = null
        )
    }
}