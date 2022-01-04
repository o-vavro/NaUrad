package com.atlasstudio.naurad.net.service

import com.atlasstudio.naurad.data.JTSKLocation
import com.atlasstudio.naurad.net.model.RuianAddressResponse
import com.atlasstudio.naurad.net.model.RuianLocationResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RuianService {

    @GET("reverseGeocode?distance=100&outSR=&f=json")
    suspend fun locationToAddress(@Query("location") latLng: JTSKLocation) : Response<RuianAddressResponse>

    @GET("findAddressCandidates?magicKey=&outSR=&maxLocations=1&outFields=&searchExtent=&f=json")
    suspend fun addressToLocation(@Query("SingleLine") address: String) : Response<RuianLocationResponse>
}