package com.atlasstudio.naurad.data

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import kotlinx.parcelize.Parcelize

@Parcelize
data class LocationWithOffices(
    @Embedded val location: TouchedLocation,
    @Relation(
        entity = Office::class,
        parentColumn = "location",
        entityColumn = "id",
        associateBy = Junction(LocationOfficeCrossRef::class, entityColumn = "officeId", parentColumn = "locationId")
    )
    val offices: List<Office>
) : Parcelable