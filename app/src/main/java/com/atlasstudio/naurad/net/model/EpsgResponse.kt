package com.atlasstudio.naurad.net.model

import com.google.gson.annotations.SerializedName

data class EpsgResponse(@SerializedName("x") val x: String?="",
                        @SerializedName("y") val y: String?="",
                        @SerializedName("z") val z: String?="")