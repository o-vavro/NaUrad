package com.atlasstudio.naurad.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface OfficeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addOffice(office: Office?)

    @Query("SELECT DISTINCT * FROM office_table WHERE id LIKE :id")
    fun getOffice(id: String): LiveData<Office?>?

    @Query("SELECT DISTINCT * FROM office_table")
    fun getOffices(): LiveData<List<Office?>?>?

    @Delete
    fun deleteOffice(office: Office?)

    @Query("DELETE FROM office_table")
    fun deleteAllOffices()
}