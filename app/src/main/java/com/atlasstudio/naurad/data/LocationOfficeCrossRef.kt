package com.atlasstudio.naurad.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Junction
import androidx.room.Relation

@Entity(primaryKeys = ["locationId", "officeId"])
data class LocationOfficeCrossRef(
    val locationId: Int,
    val officeId: String
)

data class LocationWithOffices(
    @Embedded val location: TouchedLocation,
    @Relation(
        entity = Office::class,
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(LocationOfficeCrossRef::class, entityColumn = "officeId", parentColumn = "locationId")
    )
    val offices: List<Office>
)