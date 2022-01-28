package com.atlasstudio.naurad.net.service

import com.atlasstudio.naurad.net.model.EpsgResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CoordinateTranslationService {
    @GET("trans")
    suspend fun convertLocation(@Query("x") lng: String, @Query("y") lat: String, @Query("s_srs") sourceType: Int, @Query("t_srs") targetType: Int) : Response<EpsgResponse>

    @GET("trans?s_srs=4326&t_srs=5514")
    suspend fun convertGPStoJTSK(@Query("x") lng: Double, @Query("y") lat: Double) : Response<EpsgResponse>

    @GET("trans?s_srs=5514&t_srs=4326")
    suspend fun convertJTSKtoGPS(@Query("x") lng: Double, @Query("y") lat: Double) : Response<EpsgResponse>
}