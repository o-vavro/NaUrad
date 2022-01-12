package com.atlasstudio.naurad.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TouchedLocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTouchedLocation(location: TouchedLocation?)

    @Query("SELECT DISTINCT * FROM touched_location_table WHERE id LIKE :id")
    fun getTouchedLocation(id: Int): LiveData<TouchedLocation?>?

    @Query("SELECT DISTINCT * FROM touched_location_table")
    fun getTouchedLocations(): LiveData<List<TouchedLocation?>?>?

    @Delete
    suspend fun deleteTouchedLocation(location: TouchedLocation?)

    @Query("DELETE FROM touched_location_table")
    suspend fun deleteAllTouchedLocations()
}