package com.atlasstudio.naurad.repository
import com.atlasstudio.naurad.data.OfficeDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfficeRepository @Inject constructor(private val officeDao: OfficeDao,
                                            ) {

    /*suspend fun getOffice(latLng: LatLng, officeType: OfficeType) : MutableLiveData<Office?> {
        officeDao.getOffice()
    }

    suspend fun getOffices(latLng: LatLng) : MutableLiveData<List<Office?>?> {
        offices?.let {
            for(office in offices) {
                officeDao.addOffice(office)
            }
        }
    }*/


}