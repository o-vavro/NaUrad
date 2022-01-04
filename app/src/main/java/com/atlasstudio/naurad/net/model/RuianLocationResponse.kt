package com.atlasstudio.naurad.net.model


import com.google.gson.annotations.SerializedName

data class RuianLocationResponse(
    val spatialReference: SpatialReference? = null,
    val candidates: List<Candidate> = listOf()
) {
    data class SpatialReference(
        val wkid: Int = 0,
        val latestWkid: Int? = null
    )

    data class Candidate(
        val address: String? = null,
        val location: Location? = null,
        val score: Int? = null,
        val attributes: Attributes? = null
    ) {
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

        data class Attributes(
            @SerializedName("Addr_type")
            val addrType: String? = null,
            @SerializedName("Loc_name")
            val locName: String? = null,
            @SerializedName("Type")
            val type: String? = null,
            @SerializedName("City")
            val city: String? = null,
            @SerializedName("Country")
            val country: String? = null,
            @SerializedName("Xmax")
            val xmax: Double? = null,
            @SerializedName("Xmin")
            val xmin: Double? = null,
            @SerializedName("Ymax")
            val ymax: Double? = null,
            @SerializedName("Ymin")
            val ymin: Double? = null,
            @SerializedName("Match_addr")
            val matchAddr: String? = null,
            @SerializedName("Score")
            val score: Int? = null
        )
    }
}