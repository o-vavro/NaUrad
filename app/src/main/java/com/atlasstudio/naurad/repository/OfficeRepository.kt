package com.atlasstudio.naurad.repository
import com.atlasstudio.naurad.data.JTSKLocation
import com.atlasstudio.naurad.data.Office
import com.atlasstudio.naurad.data.OfficeDao
import com.atlasstudio.naurad.net.model.*
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

    suspend fun getApiTalksForLocation(loc: LatLng) : Flow<BaseResult<String, WrappedListResponse<OfficeIdResponse>>> {
        return flow {
            val epsgResponse = epsgService.convertGPStoJTSK(loc.longitude, loc.latitude)

            if (epsgResponse.isSuccessful) {
                val epsgBody = epsgResponse.body()!!

                val ruianResponse = ruianService.locationToAddress(JTSKLocation(epsgBody.x!!, epsgBody.y!!))
                if(ruianResponse.isSuccessful) {
                    val ruianAddress = ruianResponse.body()!!.address!!.address

                    val addressParts = ruianAddress!!.split(",")
                    val psc = addressParts.last().trim().split(' ')[0]
                    val obec = addressParts.last().trim().split(' ').drop(1).joinToString(" ")
                    //val cast_obce = addressParts[1].trim()
                    val ulice = addressParts[0].trim().split(' ').dropLast(1).joinToString(" ")
                    val cisla = addressParts[0].trim().split(' ').last().split('/')
                    val popisne= cisla[0]
                    var orientacni: String = ""
                    if (cisla.size > 1) {
                        orientacni = cisla[1]
                    }

                    var filter = ""
                    if (ulice == "č.p.") {
                        filter = "{\"limit\":1,\"where\":{\"NAZEV_OBCE\":\"%s\",\"PSC\":\"%s\"}}".format(obec, psc) //,\"CISLO_DOMOVNI\":\"%s\".format(popisne)
                    }
                    else {
                        filter = "{\"limit\":1,\"where\":{\"NAZEV_OBCE\":\"%s\",\"PSC\":\"%s\",\"NAZEV_ULICE\":\"%s\"}}".format(obec, psc, ulice) //,\"CISLO_DOMOVNI\":\"%s\" , popisne)
                    }
                    if (cisla.size > 1) {
                        filter = "{\"limit\":1,\"where\":{\"NAZEV_OBCE\":\"%s\",\"PSC\":\"%s\",\"NAZEV_ULICE\":\"%s\",\"CISLO_DOMOVNI\":\"%s\",\"CISLO_ORIENTACNI\":\"%s\"}}".format(obec, psc, ulice, popisne, orientacni)
                    }

                    val officeIdResponse = apiTalksService.addressToId(filter)
                    if(officeIdResponse.isSuccessful) {
                        val officeId = officeIdResponse.body()!!.data[0].id
                        emit(BaseResult.Success(officeId))
                    }
                    else {
                        val type = object : TypeToken<WrappedListResponse<OfficeIdResponse>>() {}.type
                        val err = Gson().fromJson<WrappedListResponse<OfficeIdResponse>>(
                            epsgResponse.errorBody()!!.charStream(), type
                        )!!
                        err.code = officeIdResponse.code()
                        emit(BaseResult.Error(err))
                    }
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
    }

    suspend fun getOfficesForLocation(loc: LatLng) : Flow<BaseResult<List<Office?>, WrappedListResponse<OfficeResponse>>> {
        return flow {
            val epsgResponse = epsgService.convertGPStoJTSK(loc.longitude, loc.latitude)

            if (epsgResponse.isSuccessful) {
                val epsgBody = epsgResponse.body()!!

                val ruianResponse = ruianService.locationToAddress(JTSKLocation(epsgBody.x!!, epsgBody.y!!))
                if(ruianResponse.isSuccessful) {
                    val ruianAddress = ruianResponse.body()!!.address!!.address

                    val addressParts = ruianAddress!!.split(",")
                    val psc = addressParts.last().trim().split(' ')[0]
                    val obec = addressParts.last().trim().split(' ').drop(1).joinToString(" ")
                    //val cast_obce = addressParts[1].trim()
                    val ulice = addressParts[0].trim().split(' ').dropLast(1).joinToString(" ")
                    val cisla = addressParts[0].trim().split(' ').last().split('/')
                    val popisne= cisla[0]
                    var orientacni: String = ""
                    if (cisla.size > 1) {
                        orientacni = cisla[1]
                    }

                    var filter = ""
                    if (ulice == "č.p.") {
                        filter = "{\"limit\":1,\"where\":{\"NAZEV_OBCE\":\"%s\",\"PSC\":\"%s\"}}".format(obec, psc) //,\"CISLO_DOMOVNI\":\"%s\".format(popisne)
                    }
                    else {
                        filter = "{\"limit\":1,\"where\":{\"NAZEV_OBCE\":\"%s\",\"PSC\":\"%s\",\"NAZEV_ULICE\":\"%s\"}}".format(obec, psc, ulice) //,\"CISLO_DOMOVNI\":\"%s\" , popisne)
                    }
                    if (cisla.size > 1) {
                        filter = "{\"limit\":1,\"where\":{\"NAZEV_OBCE\":\"%s\",\"PSC\":\"%s\",\"NAZEV_ULICE\":\"%s\",\"CISLO_DOMOVNI\":\"%s\",\"CISLO_ORIENTACNI\":\"%s\"}}".format(obec, psc, ulice, popisne, orientacni)
                    }

                    val officeIdResponse = apiTalksService.addressToId(filter)
                    if(officeIdResponse.isSuccessful) {
                        val officeId = officeIdResponse.body()!!.data[0].id

                        val officeResponse = apiTalksService.idToOffice(officeId)
                        if(officeResponse.isSuccessful) {
                            emit(BaseResult.Success(officeResponse.body()!!.toOffices()))
                        }
                        else {
                            val type = object : TypeToken<WrappedListResponse<OfficeResponse>>() {}.type
                            val err = Gson().fromJson<WrappedListResponse<OfficeResponse>>(
                                officeResponse.errorBody()!!.charStream(), type
                            )!!
                            err.code = officeResponse.code()
                            emit(BaseResult.Error(err))
                        }
                    }
                    else {
                        val type = object : TypeToken<WrappedListResponse<OfficeIdResponse>>() {}.type
                        val err = Gson().fromJson<WrappedListResponse<OfficeResponse>>(
                            officeIdResponse.errorBody()!!.charStream(), type
                        )!!
                        err.code = officeIdResponse.code()
                        emit(BaseResult.Error(err))
                    }
                } else {
                    val type = object : TypeToken<WrappedListResponse<RuianAddressResponse>>() {}.type
                    val err = Gson().fromJson<WrappedListResponse<OfficeResponse>>(
                        ruianResponse.errorBody()!!.charStream(), type
                    )!!
                    err.code = ruianResponse.code()
                    emit(BaseResult.Error(err))
                }
            } else {
                val type = object : TypeToken<WrappedListResponse<EpsgResponse>>() {}.type
                val err = Gson().fromJson<WrappedListResponse<OfficeResponse>>(
                    epsgResponse.errorBody()!!.charStream(), type
                )!!
                err.code = epsgResponse.code()
                emit(BaseResult.Error(err))
            }
        }
    }

    suspend fun getLocatedOfficesForLocation(loc: LatLng) : Flow<BaseResult<List<Office?>, WrappedListResponse<OfficeResponse>>> {
        return flow {
            val epsgResponse = epsgService.convertGPStoJTSK(loc.longitude, loc.latitude)

            if (epsgResponse.isSuccessful) {
                val epsgBody = epsgResponse.body()!!

                val ruianResponse = ruianService.locationToAddress(JTSKLocation(epsgBody.x!!, epsgBody.y!!))
                if(ruianResponse.isSuccessful) {
                    val ruianAddress = ruianResponse.body()!!.address!!.address

                    val addressParts = ruianAddress!!.split(",")
                    val psc = addressParts.last().trim().split(' ')[0]
                    val obec = addressParts.last().trim().split(' ').drop(1).joinToString(" ")
                    //val cast_obce = addressParts[1].trim()
                    val ulice = addressParts[0].trim().split(' ').dropLast(1).joinToString(" ")
                    val cisla = addressParts[0].trim().split(' ').last().split('/')
                    val popisne= cisla[0]
                    var orientacni: String = ""
                    if (cisla.size > 1) {
                        orientacni = cisla[1]
                    }

                    var filter = ""
                    if (ulice == "č.p.") {
                        filter = "{\"limit\":1,\"where\":{\"NAZEV_OBCE\":\"%s\",\"PSC\":\"%s\"}}".format(obec, psc) //,\"CISLO_DOMOVNI\":\"%s\".format(popisne)
                    }
                    else {
                        filter = "{\"limit\":1,\"where\":{\"NAZEV_OBCE\":\"%s\",\"PSC\":\"%s\",\"NAZEV_ULICE\":\"%s\"}}".format(obec, psc, ulice) //,\"CISLO_DOMOVNI\":\"%s\" , popisne)
                    }
                    if (cisla.size > 1) {
                        filter = "{\"limit\":1,\"where\":{\"NAZEV_OBCE\":\"%s\",\"PSC\":\"%s\",\"NAZEV_ULICE\":\"%s\",\"CISLO_DOMOVNI\":\"%s\",\"CISLO_ORIENTACNI\":\"%s\"}}".format(obec, psc, ulice, popisne, orientacni)
                    }

                    val officeIdResponse = apiTalksService.addressToId(filter)
                    if(officeIdResponse.isSuccessful) {
                        val officeId = officeIdResponse.body()!!.data[0].id

                        val officeResponse = apiTalksService.idToOffice(officeId)
                        if(officeResponse.isSuccessful) {
                            var offices = officeResponse.body()!!.toOffices()

                            for(i in offices.indices) {
                                val officeLocationResponse = ruianService.addressToLocation(offices[i]!!.address)

                                if(officeLocationResponse.isSuccessful) {
                                    val loc = officeLocationResponse.body()!!.candidates[0].location

                                    val officeGPSLocResponse = epsgService.convertJTSKtoGPS(loc!!.x, loc!!.y)

                                    if(officeGPSLocResponse.isSuccessful) {
                                        val officeGPSLoc = officeGPSLocResponse.body()
                                        offices[i]?.location = LatLng(officeGPSLoc!!.y!!.toDouble(),
                                                                      officeGPSLoc!!.x!!.toDouble())
                                    } else {
                                        val type = object : TypeToken<WrappedListResponse<EpsgResponse>>() {}.type
                                        val err = Gson().fromJson<WrappedListResponse<OfficeResponse>>(
                                            officeGPSLocResponse.errorBody()!!.charStream(), type
                                        )!!
                                        err.code = officeGPSLocResponse.code()
                                        emit(BaseResult.Error(err))
                                    }
                                } else {
                                    val type = object : TypeToken<WrappedListResponse<RuianLocationResponse>>() {}.type
                                    val err = Gson().fromJson<WrappedListResponse<OfficeResponse>>(
                                        officeLocationResponse.errorBody()!!.charStream(), type
                                    )!!
                                    err.code = officeLocationResponse.code()
                                    emit(BaseResult.Error(err))
                                }
                            }

                            emit(BaseResult.Success(offices))
                        }
                        else {
                            val type = object : TypeToken<WrappedListResponse<OfficeResponse>>() {}.type
                            val err = Gson().fromJson<WrappedListResponse<OfficeResponse>>(
                                officeResponse.errorBody()!!.charStream(), type
                            )!!
                            err.code = officeResponse.code()
                            emit(BaseResult.Error(err))
                        }
                    }
                    else {
                        val type = object : TypeToken<WrappedListResponse<OfficeIdResponse>>() {}.type
                        val err = Gson().fromJson<WrappedListResponse<OfficeResponse>>(
                            officeIdResponse.errorBody()!!.charStream(), type
                        )!!
                        err.code = officeIdResponse.code()
                        emit(BaseResult.Error(err))
                    }
                } else {
                    val type = object : TypeToken<WrappedListResponse<RuianAddressResponse>>() {}.type
                    val err = Gson().fromJson<WrappedListResponse<OfficeResponse>>(
                        ruianResponse.errorBody()!!.charStream(), type
                    )!!
                    err.code = ruianResponse.code()
                    emit(BaseResult.Error(err))
                }
            } else {
                val type = object : TypeToken<WrappedListResponse<EpsgResponse>>() {}.type
                val err = Gson().fromJson<WrappedListResponse<OfficeResponse>>(
                    epsgResponse.errorBody()!!.charStream(), type
                )!!
                err.code = epsgResponse.code()
                emit(BaseResult.Error(err))
            }
        }
    }
}