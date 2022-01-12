package com.atlasstudio.naurad.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AllDao {
    @Transaction
    @Query("SELECT DISTINCT * FROM touched_location_table WHERE id LIKE :id")
    fun getTouchedLocationWithOffices(id: Int): LiveData<LocationWithOffices?>?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLocationOfficeCrossRef(locationOfficeCrossRef: LocationOfficeCrossRef)
}