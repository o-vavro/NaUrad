package com.atlasstudio.naurad.repository
import com.atlasstudio.naurad.data.JTSKLocation
import com.atlasstudio.naurad.data.OfficeDao
import com.atlasstudio.naurad.net.model.EpsgResponse
import com.atlasstudio.naurad.net.model.RuianAddressResponse
import com.atlasstudio.naurad.net.service.ApiTalksService
import com.atlasstudio.naurad.net.service.EpsgService
import com.atlasstudio.naurad.net.service.RuianService
import com.atlasstudio.naurad.utils.BaseResult
import com.atlasstudio.naurad.utils.WrappedListResponse
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfficeRepository @Inject constructor(private val officeDao: OfficeDao,
                                           private val epsgService: EpsgService,
                                           private val ruianService: RuianService,
                                           private val apiTalksService: ApiTalksService) {

    /*suspend fun getOffice(latLng: LatLng, officeType: OfficeType) : Flow<BaseResult<Office, WrappedListResponse<OfficeResponse>>> {
        // if(officeDao.getOffice(latLng))
        // else...

    }

    suspend fun getOffices(latLng: LatLng) : Flow<BaseResult<List<Office>, WrappedListResponse<OfficeResponse>>> {
        /*offices?.let {
            for(office in offices) {
                officeDao.addOffice(office)
            }
        }*/
    }*/

    suspend fun getJTSKLocation(loc: LatLng): Flow<BaseResult<List<Double>, WrappedListResponse<EpsgResponse>>> {
        return flow {
            val response = epsgService.convertGPStoJTSK(loc.longitude, loc.latitude)

            if (response.isSuccessful) {
                val body = response.body()!!
                emit(BaseResult.Success(listOf(body.x!!.toDouble(), body.y!!.toDouble(), body.z!!.toDouble())))
            } else {
                val type = object : TypeToken<WrappedListResponse<EpsgResponse>>() {}.type
                val err = Gson().fromJson<WrappedListResponse<EpsgResponse>>(
                    response.errorBody()!!.charStream(), type
                )!!
                err.code = response.code()
                emit(BaseResult.Error(err))
            }
        }
    }

    suspend fun getAddressForLocation(loc: LatLng): Flow<BaseResult<String, WrappedListResponse<RuianAddressResponse>>> {
        return flow {
            val epsgResponse = epsgService.convertGPStoJTSK(loc.longitude, loc.latitude)

            if (epsgResponse.isSuccessful) {
                val epsgBody = epsgResponse.body()!!

                val ruianResponse = ruianService.locationToAddress(JTSKLocation(epsgBody.x!!, epsgBody.y!!))
                if(ruianResponse.isSuccessful) {
                    val ruianBody = ruianResponse.body()!!
                    emit(BaseResult.Success(ruianBody.address!!.address!!))
                } else {
                    val type = object : TypeToken<WrappedListResponse<RuianAddressResponse>>() {}.type
                    val err = Gson().fromJson<WrappedListResponse<RuianAddressResponse>>(
                        epsgResponse.errorBody()!!.charStream(), type
                    )!!
                    err.code = ruianResponse.code()
                    emit(BaseResult.Error(err))
                }
            } else {
                val type = object : TypeToken<WrappedListResponse<EpsgResponse>>() {}.type
                val err = Gson().fromJson<WrappedListResponse<RuianAddressResponse>>(
                    epsgResponse.errorBody()!!.charStream(), type
                )!!
                err.code = epsgResponse.code()
                emit(BaseResult.Error(err))
            }
        }
    }

    /*suspend fun getApiTalksForLocation(loc: LatLng) : Flow<BaseResult<String, WrappedListResponse<OfficeIdResponse>>> {
        return flow {
            val epsgResponse = epsgService.convertGPStoJTSK(loc.longitude, loc.latitude)

            if (epsgResponse.isSuccessful) {
                val epsgBody = epsgResponse.body()!!

                val ruianResponse = ruianService.locationToAddress(JTSKLocation(epsgBody.x!!, epsgBody.y!!))
                if(ruianResponse.isSuccessful) {
                    val ruianBody = ruianResponse.body()!!

                    //val apiTalksResponse = apiTalksService.addressToId()

                    emit(BaseResult.Success(ruianBody.address!!.address!!))
                } else {
                    val type = object : TypeToken<WrappedListResponse<RuianAddressResponse>>() {}.type
                    val err = Gson().fromJson<WrappedListResponse<OfficeIdResponse>>(
                        epsgResponse.errorBody()!!.charStream(), type
                    )!!
                    err.code = ruianResponse.code()
                    emit(BaseResult.Error(err))
                }
            } else {
                val type = object : TypeToken<WrappedListResponse<EpsgResponse>>() {}.type
                val err = Gson().fromJson<WrappedListResponse<OfficeIdResponse>>(
                    epsgResponse.errorBody()!!.charStream(), type
                )!!
                err.code = epsgResponse.code()
                emit(BaseResult.Error(err))
            }
        }
    }*/
}