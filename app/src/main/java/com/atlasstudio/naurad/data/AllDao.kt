package com.atlasstudio.naurad.data

import androidx.room.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow

@Dao
interface AllDao {
    @Transaction
    @Query("SELECT DISTINCT * FROM touched_location_table WHERE location LIKE :loc")
    fun getTouchedLocationWithOffices(loc: LatLng): Flow<List<LocationWithOffices>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLocationOfficeCrossRef(locationOfficeCrossRef: LocationOfficeCrossRef)
}