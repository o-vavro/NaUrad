package com.atlasstudio.naurad.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.parcel.Parcelize
import java.time.LocalDateTime

@Parcelize
public enum class OfficeType : Parcelable {
        TaxOffice,
        LabourOffice,
        DistrictCourt,
        RegionalCourt,
        HighCourt,
        CustomsOffice,
        CityGovernmentOffice
}

@Parcelize
data class OfficeInfo (
        val officeHours : Array<LocalDateTime>,
        val phoneNumber : String,
        val note : String
        ) : Parcelable {
}

@Entity(tableName= "office_table")
@Parcelize
data class Office (
        val name : String,
        val location : LatLng,
        val type : OfficeType,
        val info : OfficeInfo,
        @PrimaryKey(autoGenerate = true) val id : Int = 0
        ) : Parcelable {
}