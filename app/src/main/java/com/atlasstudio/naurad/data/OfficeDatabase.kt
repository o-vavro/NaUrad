package com.atlasstudio.naurad.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.atlasstudio.naurad.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Provider


@TypeConverters(LatLngConverter::class, OfficeInfoConverter::class)
@Database(entities = [Office::class], version = 1)
abstract class OfficeDatabase: RoomDatabase() {
    abstract fun officeDao(): OfficeDao

    class Callback @Inject constructor(
        private val database: Provider<OfficeDatabase>,
        @ApplicationScope private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback()
}