package com.atlasstudio.naurad.data

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize

@Entity(tableName="touched_location_table")
@Parcelize
data class TouchedLocation(
    @PrimaryKey(autoGenerate = true)
    @NonNull
    val id : Int = 0,
    var location : LatLng,
    var address: String
    ) : Parcelable