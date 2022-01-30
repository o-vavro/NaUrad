package com.atlasstudio.naurad.net.model

import com.google.gson.annotations.SerializedName

data class CoordinateTranslationResponse(@SerializedName("Coordinates") val coords: String?="")