package com.atlasstudio.naurad.net.service

import com.atlasstudio.naurad.net.model.CoordinateTranslationResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface CoordinateTranslationService {
    @FormUrlEncoded
    @POST("WCTSHandlerhld.ashx")
    suspend fun convertLocationBase(
        @Field("source") source: String,
        @Field("sourceSRS") sourceSRS: String,
        @Field("targetSRS") targetSRS: String,
        @Field("sourceXYorder") sourceXYOrder: String,
        @Field("targetXYorder") targetXYOrder: String,
        @Field("sourceSixtiethView") sourceSixtiethView: Boolean,
        @Field("targetSixtiethView") targetSixtiethView: Boolean,
        @Field("coordinates", encoded = true) coordinates: String,
        @Field("time") date: String
    ): Response<CoordinateTranslationResponse>
}

suspend fun CoordinateTranslationService.convertLocation(
    sourceSRS: String,
    targetSRS: String,
    sourceXYOrder: String,
    targetXYOrder: String,
    coordinates: String,
    date: String
): Response<CoordinateTranslationResponse> {
    return this.convertLocationBase(
        "Coordinates",
        sourceSRS,
        targetSRS,
        sourceXYOrder,
        targetXYOrder,
        false,
        false,
        coordinates,
        date
    )
}

suspend fun CoordinateTranslationService.convertLocationGPSToJTSK(
    coordinates: String,
    date: String
): Response<CoordinateTranslationResponse> {
    return convertLocation(
        "urn:ogc:def:crs:EPSG::4979",
        "urn:ogc:def:crs,crs:EPSG::5514,crs:EPSG::5705",
        "yx",
        "xy",
        coordinates,
        date
    )
}

suspend fun CoordinateTranslationService.convertLocationJTSKToGPS(
    coordinates: String,
    date: String
): Response<CoordinateTranslationResponse> {
    return convertLocation(
        "urn:ogc:def:crs,crs:EPSG::5514,crs:EPSG::5705",
        "urn:ogc:def:crs:EPSG::4979",
        "xy",
        "yx",
        coordinates,
        date
    )
}