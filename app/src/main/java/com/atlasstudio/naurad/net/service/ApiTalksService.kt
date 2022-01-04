package com.atlasstudio.naurad.net.service

import com.atlasstudio.naurad.net.model.OfficeIdResponse
import com.atlasstudio.naurad.net.model.OfficeResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiTalksService {

    @GET("spadovost?")
    suspend fun addressToId(@Query("filter") filter: String) : Response<OfficeIdResponse>

    @GET("spadovost/{officeId}")
    suspend fun idToOffice(@Path("officeId") officeId: String) : Response<OfficeResponse>

    @GET("spadovost/{allId}")
    suspend fun idToAllOffices(@Path("allId") allId: String) : Response<OfficeResponse>
}