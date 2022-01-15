package com.atlasstudio.naurad.data

import androidx.room.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow

@Dao
interface AllDao {
    @Transaction
    @Query("SELECT DISTINCT * FROM touched_location_table WHERE location LIKE :loc")
    fun getTouchedLocationWithOffices(loc: LatLng): Flow<List<LocationWithOffices>>

    @Query("SELECT DISTINCT * FROM location_office_cross_ref WHERE officeId LIKE :id")
    fun getCrossRefsForOffice(id: String): Flow<List<LocationOfficeCrossRef>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLocationOfficeCrossRef(locationOfficeCrossRef: LocationOfficeCrossRef)

    @Query("DELETE FROM location_office_cross_ref WHERE locationId LIKE :locationId AND officeId LIKE :officeId")
    suspend fun deleteLocationOfficeCrossRef(locationId: LatLng, officeId: String)
}