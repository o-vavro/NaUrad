package com.atlasstudio.naurad.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.atlasstudio.naurad.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Provider


@TypeConverters(LatLngConverter::class, OfficeInfoConverter::class)
@Database(entities = [Office::class, TouchedLocation::class, LocationOfficeCrossRef::class], version = 1)
abstract class LocationOfficeDatabase: RoomDatabase() {
    abstract fun officeDao(): OfficeDao
    abstract fun locationDao(): TouchedLocationDao
    abstract fun allDao(): AllDao

    class Callback @Inject constructor(
        private val databaseLocation: Provider<LocationOfficeDatabase>,
        @ApplicationScope private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback()
}