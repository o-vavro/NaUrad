package com.atlasstudio.naurad.repository
import com.atlasstudio.naurad.data.*
import com.atlasstudio.naurad.net.model.CoordinateTranslationResponse
import com.atlasstudio.naurad.net.model.OfficeIdResponse
import com.atlasstudio.naurad.net.model.OfficeResponse
import com.atlasstudio.naurad.net.model.RuianAddressResponse
import com.atlasstudio.naurad.net.service.*
import com.atlasstudio.naurad.net.utils.ErrorResponseType
import com.atlasstudio.naurad.utils.BaseResult
import com.atlasstudio.naurad.utils.WrappedListResponse
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationOfficeRepository @Inject constructor(private val officeDao: OfficeDao,
                                                   private val locationDao: TouchedLocationDao,
                                                   private val locationWithOfficesDao: LocationWithOfficesDao,
                                                   private val coordinateTranslationService: CoordinateTranslationService,
                                                   private val ruianService: RuianService,
                                                   private val apiTalksService: ApiTalksService) {

    suspend fun getJTSKLocation(loc: LatLng): Flow<BaseResult<List<Double>, WrappedListResponse<CoordinateTranslationResponse>>> {
        return flow {
            val response = coordinateTranslationService.convertLocationGPSToJTSK(
                "${loc.longitude}+${loc.latitude}+0",
                SimpleDateFormat("yyyy-MM-dd").format(Date())
            )

            if (response.isSuccessful) {
                val body = response.body()!!
                val bodyParts = body.coords!!.split(" ")
                emit(BaseResult.Success(listOf(bodyParts[0]!!.toDouble(), bodyParts[1]!!.toDouble(), bodyParts[2]!!.toDouble())))
            } else {
                val type = object : TypeToken<WrappedListResponse<CoordinateTranslationResponse>>() {}.type
                val err = Gson().fromJson<WrappedListResponse<CoordinateTranslationResponse>>(
                    response.errorBody()!!.charStream(), type
                )!!
                err.code = response.code()
                emit(BaseResult.Error(err))
            }
        }
    }

    suspend fun getAddressForLocation(loc: LatLng): Flow<BaseResult<String, WrappedListResponse<RuianAddressResponse>>> {
        return flow {
            val coordinateTranslationResponse = coordinateTranslationService.convertLocationGPSToJTSK(
                "${loc.longitude}+${loc.latitude}+0",
                SimpleDateFormat("yyyy-MM-dd").format(Date())
            )

            if (coordinateTranslationResponse.isSuccessful) {
                val body = coordinateTranslationResponse.body()!!
                val bodyParts = body.coords!!.split(" ")

                val ruianResponse = ruianService.locationToAddress(JTSKLocation(bodyParts[0]!!, bodyParts[1]!!))
                if(ruianResponse.isSuccessful) {
                    val ruianBody = ruianResponse.body()!!
                    emit(BaseResult.Success(ruianBody.address!!.address!!))
                } else {
                    val type = object : TypeToken<WrappedListResponse<RuianAddressResponse>>() {}.type
                    val err = Gson().fromJson<WrappedListResponse<RuianAddressResponse>>(
                        coordinateTranslationResponse.errorBody()!!.charStream(), type
                    )!!
                    err.code = ruianResponse.code()
                    emit(BaseResult.Error(err))
                }
            } else {
                val type = object : TypeToken<WrappedListResponse<CoordinateTranslationResponse>>() {}.type
                val err = Gson().fromJson<WrappedListResponse<RuianAddressResponse>>(
                    coordinateTranslationResponse.errorBody()!!.charStream(), type
                )!!
                err.code = coordinateTranslationResponse.code()
                emit(BaseResult.Error(err))
            }
        }
    }

    suspend fun getApiTalksForLocation(loc: LatLng) : Flow<BaseResult<String, WrappedListResponse<OfficeIdResponse>>> {
        return flow {
            val coordinateTranslationResponse = coordinateTranslationService.convertLocationGPSToJTSK(
                "${loc.longitude}+${loc.latitude}+0",
                SimpleDateFormat("yyyy-MM-dd").format(Date())
            )

            if (coordinateTranslationResponse.isSuccessful) {
                val body = coordinateTranslationResponse.body()!!
                val bodyParts = body.coords!!.split(" ")

                val ruianResponse = ruianService.locationToAddress(JTSKLocation(bodyParts[0]!!, bodyParts[1]!!))
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
                            coordinateTranslationResponse.errorBody()!!.charStream(), type
                        )!!
                        err.code = officeIdResponse.code()
                        emit(BaseResult.Error(err))
                    }
                } else {
                    val type = object : TypeToken<WrappedListResponse<RuianAddressResponse>>() {}.type
                    val err = Gson().fromJson<WrappedListResponse<OfficeIdResponse>>(
                        coordinateTranslationResponse.errorBody()!!.charStream(), type
                    )!!
                    err.code = ruianResponse.code()
                    emit(BaseResult.Error(err))
                }
            } else {
                val type = object : TypeToken<WrappedListResponse<CoordinateTranslationResponse>>() {}.type
                val err = Gson().fromJson<WrappedListResponse<OfficeIdResponse>>(
                    coordinateTranslationResponse.errorBody()!!.charStream(), type
                )!!
                err.code = coordinateTranslationResponse.code()
                emit(BaseResult.Error(err))
            }
        }
    }

    suspend fun getOfficesForLocation(loc: LatLng) : Flow<BaseResult<List<Office?>, WrappedListResponse<OfficeResponse>>> {
        return flow {
            val coordinateTranslationResponse = coordinateTranslationService.convertLocationGPSToJTSK(
                "${loc.longitude}+${loc.latitude}+0",
                SimpleDateFormat("yyyy-MM-dd").format(Date())
            )

            if (coordinateTranslationResponse.isSuccessful) {
                val body = coordinateTranslationResponse.body()!!
                val bodyParts = body.coords!!.split(" ")

                val ruianResponse = ruianService.locationToAddress(JTSKLocation(bodyParts[0]!!, bodyParts[1]!!))
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
                val type = object : TypeToken<WrappedListResponse<CoordinateTranslationResponse>>() {}.type
                val err = Gson().fromJson<WrappedListResponse<OfficeResponse>>(
                    coordinateTranslationResponse.errorBody()!!.charStream(), type
                )!!
                err.code = coordinateTranslationResponse.code()
                emit(BaseResult.Error(err))
            }
        }
    }

    suspend fun getLocatedOfficesForLocation(loc: LatLng) : Flow<BaseResult<LocationWithOffices, ErrorResponseType>> {
        return flow {
            //val databaseResult = allDao.getTouchedLocationWithOffices(loc)

            val coordinateTranslationResponse = coordinateTranslationService.convertLocationGPSToJTSK(
                "${loc.longitude}+${loc.latitude}+0",
                SimpleDateFormat("yyyy-MM-dd").format(Date())
            )

            if (coordinateTranslationResponse.isSuccessful) {
                val body = coordinateTranslationResponse.body()!!
                val bodyParts = body.coords!!.split(" ")

                val ruianResponse = ruianService.locationToAddress(JTSKLocation(bodyParts[0]!!, bodyParts[1]!!))
                if(ruianResponse.isSuccessful && ruianResponse.body()!!.address != null) {
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

                    var addressFilter = ""
                    if (ulice == "č.p.") {
                        addressFilter = "{\"limit\":1,\"where\":{\"NAZEV_OBCE\":\"%s\",\"PSC\":\"%s\"}}".format(obec, psc) //,\"CISLO_DOMOVNI\":\"%s\".format(popisne)
                    }
                    else {
                        addressFilter = "{\"limit\":1,\"where\":{\"NAZEV_OBCE\":\"%s\",\"PSC\":\"%s\",\"NAZEV_ULICE\":\"%s\"}}".format(obec, psc, ulice) //,\"CISLO_DOMOVNI\":\"%s\" , popisne)
                    }
                    if (cisla.size > 1) {
                        addressFilter = "{\"limit\":1,\"where\":{\"NAZEV_OBCE\":\"%s\",\"PSC\":\"%s\",\"NAZEV_ULICE\":\"%s\",\"CISLO_DOMOVNI\":\"%s\",\"CISLO_ORIENTACNI\":\"%s\"}}".format(obec, psc, ulice, popisne, orientacni)
                    }

                    val officeIdResponse = apiTalksService.addressToId(addressFilter)
                    if(officeIdResponse.isSuccessful) {
                        val officeId = officeIdResponse.body()!!.data[0].id

                        val officeResponse = apiTalksService.idToOffice(officeId)
                        if(officeResponse.isSuccessful) {
                            var officesNulled = officeResponse.body()!!.toOffices()
                            var offices : MutableList<Office> = mutableListOf()

                            for(office in officesNulled) {
                                office?.let {
                                    val officeLocationResponse =
                                        ruianService.addressToLocation(office.address)

                                    if (officeLocationResponse.isSuccessful) {
                                        val loc =
                                            officeLocationResponse.body()!!.candidates[0].location

                                        val officeGPSLocResponse =
                                            coordinateTranslationService.convertLocationJTSKToGPS(
                                                "${loc!!.x}+${loc!!.y}+0",
                                                SimpleDateFormat("yyyy-MM-dd").format(Date())
                                            )

                                        if (officeGPSLocResponse.isSuccessful) {
                                            val officeGPSLoc = officeGPSLocResponse.body()!!.coords!!.split(' ')
                                            office.location = LatLng(officeGPSLoc[1]!!.toDouble(), officeGPSLoc[0]!!.toDouble())
                                            offices.add(office)
                                        } else {
                                            emit(BaseResult.Error(ErrorResponseType.LocationForOfficeError))
                                        }
                                    } else {
                                        emit(BaseResult.Error(ErrorResponseType.AddressForOfficeError))
                                    }
                                }
                            }

                            emit(BaseResult.Success(LocationWithOffices(TouchedLocation(loc, ruianAddress), offices)))
                        }
                        else {
                            emit(BaseResult.Error(ErrorResponseType.OfficesNotFoundError))
                        }
                    }
                    else {
                        emit(BaseResult.Error(ErrorResponseType.OfficesNotFoundError))
                    }
                } else {
                    emit(BaseResult.Error(ErrorResponseType.AddressForLocationError))
                }
            } else {
                emit(BaseResult.Error(ErrorResponseType.LocationTranslationError))
            }
        }
    }

    suspend fun storeLocation(location: LocationWithOffices) {
        location.location?.let {
            locationDao.addTouchedLocation(it)
            for (office in location.offices) {
                office?.let { office ->
                    officeDao.addOffice(office)
                    locationWithOfficesDao.insertLocationOfficeCrossRef(
                        LocationOfficeCrossRef(
                            it.location,
                            office.id
                        )
                    )
                }
            }
        }
    }

    suspend fun deleteLocation(location: LocationWithOffices) {
        location.location?.let {
            locationDao.deleteTouchedLocation(it.location, it.address)
            // delete all offices with only one ref equal to this touched location and the ref
            for (office in location.offices) {
                office?.let {
                    locationWithOfficesDao.getCrossRefsForOffice(office.id)
                        .collect { crossRefList ->
                            if (crossRefList.size == 1) {
                                officeDao.deleteOffice(office)
                            }
                        }
                    locationWithOfficesDao.deleteLocationOfficeCrossRef(it.location, office.id)
                }
            }
        }
    }

    suspend fun deleteLocation(location: TouchedLocation) {
        location.location?.let {
            locationDao.deleteTouchedLocation(it, location.address)
            // delete all offices with only one ref equal to this touched location and the ref
            locationWithOfficesDao.getCrossRefsForLocation(it)
                .collect { crossRefListLocation ->
                    for (item in crossRefListLocation) {
                        locationWithOfficesDao.getCrossRefsForOffice(item.officeId)
                            .collect { crossRefListOffice ->
                                if (crossRefListOffice.size == 1) {
                                    officeDao.getOffice(item.officeId)?.value?.let { office ->
                                        officeDao.deleteOffice(office)
                                    }

                                }
                            }
                    }
                }
        }
    }

    suspend fun isLocationStored(location: LatLng?): Flow<Boolean> = flow {
        location?.let {
            locationDao.getTouchedLocation(location)
                .collect {
                    emit(it != null)
                }
        }
        emit(false)
    }

    suspend fun getFavouriteLocations(): Flow<List<TouchedLocation>> = flow {
        locationDao.getTouchedLocations()
            .collect {
                emit(it)
            }
    }

    suspend fun getLocatedOfficesForFavourite(location: TouchedLocation) = flow {
        locationWithOfficesDao.getTouchedLocationWithOffices(location.location)
            .collect {
                emit(it)
            }
    }
}