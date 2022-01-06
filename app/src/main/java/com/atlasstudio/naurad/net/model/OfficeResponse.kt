package com.atlasstudio.naurad.net.model

import com.atlasstudio.naurad.data.Office
import com.atlasstudio.naurad.data.OfficeInfo
import com.atlasstudio.naurad.data.OfficeType
import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName

data class OfficeResponse(
    val id: String = "",

    @SerializedName("ZUJ")
    val zuj: String? = null,

    @SerializedName("OKRESNI_SOUD_APITALKS_ID")
    val okresniSoudApiTalksId: String? = null,
    @SerializedName("KRAJSKY_SOUD_APITALKS_ID")
    val krajskySoudApiTalksId: String? = null,
    @SerializedName("VRCHNI_SOUD_APITALKS_ID")
    val vrchniSoudApiTalksId: String? = null,
    @SerializedName("CELNI_SPRAVA_APITALKS_ID")
    val celniSpravaApiTalksId: String? = null,
    @SerializedName("FINANCNI_URAD_APITALKS_ID")
    val financniUradApiTalksId: String? = null,
    @SerializedName("CSSZ_APITALKS_ID")
    val csszApiTalksId: String? = null,
    @SerializedName("URAD_APITALKS_ID")
    val obecniUradApiTalksId: String? = null,

    @SerializedName("KOD_MOMC")
    val kodMomc: String? = null,
    @SerializedName("KOD_CASTI_OBCE")
    val kodCastiObce: String? = null,
    @SerializedName("KOD_ADM")
    val kodAdm: String? = null,
    @SerializedName("KOD_OBCE")
    val kodObce: String? = null,
    @SerializedName("KOD_MOP")
    val kodMop: String? = null,
    @SerializedName("KOD_ULICE")
    val kodUlice: String? = null,

    @SerializedName("NAZEV_OBCE")
    val nazevObce: String? = null,
    @SerializedName("NAZEV_MOMC")
    val nazevMomc: String? = null,
    @SerializedName("NAZEV_MOP")
    val nazevMop: String? = null,
    @SerializedName("NAZEV_CASTI_OBCE")
    val nazevCastiObce: String? = null,
    @SerializedName("NAZEV_ULICE")
    val nazevUlice: String? = null,
    @SerializedName("TYP_SO")
    val typSo: String? = null,
    @SerializedName("CISLO_DOMOVNI")
    val cisloDomovni: String? = null,
    @SerializedName("CISLO_ORIENTACNI")
    val cisloOrientacni: String? = null,
    @SerializedName("ZNAK_CISLA_ORIENTACNIHO")
    val znakCislaOrientacniho: String? = null,
    @SerializedName("PSC")
    val psc: String? = null,
    @SerializedName("SOURADNICE_X")
    val souradniceX: String? = null,
    @SerializedName("SOURADNICE_Y")
    val souradniceY: String? = null,

    val soudy: Soudy? = null,
    val celniSprava: Urad? = null,
    val financiUrad: FinanciUrad? = null,
    @SerializedName("CSZZ")
    val cssz: Urad? = null,
    @SerializedName("urad")
    val obecniUrad: Urad? = null,

    @SerializedName("PLATI_OD")
    val platiOd: String? = null
) {
    data class Urad(
        val id: String = "",

        @SerializedName("ICO")
        val ico: String? = null,
        @SerializedName("Zkratka")
        val zkratka: String? = null,
        @SerializedName("Nazev")
        val nazev: String? = null,
        @SerializedName("AdresaUradu")
        val adresaUradu: AdresaUradu? = null,
        @SerializedName("Email")
        val email: Email? = null,

        @SerializedName("TypSubjektu")
        val typSubjektu: String? = null,
        @SerializedName("PravniForma")
        val pravniForma: String? = null,
        @SerializedName("PrimarniOvm")
        val primarniOvm: String? = null,

        @SerializedName("IdDS")
        val idDs: String? = null,
        @SerializedName("TypDS")
        val typDs: String? = null,
        @SerializedName("StavDS")
        val stavDs: String? = null,

        @SerializedName("StavSubjektu")
        val stavSubjektu: String? = null,
        @SerializedName("DetailSubjektu")
        val detailSubjektu: String? = null,

        @SerializedName("IdentifikatorOvm")
        val identifikatorOvm: String? = null,
        @SerializedName("KategorieOvm")
        val kategorieOvm: String? = null
    ) {
        data class AdresaUradu(
            @SerializedName("AdresniBod")
            val adresniBod: String = "",

            @SerializedName("UliceNazev")
            val uliceNazev: String? = null,
            @SerializedName("CisloDomovni")
            val cisloDomovni: String? = null,
            @SerializedName("CisloOrientacni")
            val cisloOrientacni: String? = null,
            @SerializedName("ObecNazev")
            val obecNazev: String? = null,
            @SerializedName("ObecKod")
            val obecKod: String? = null,
            @SerializedName("CastObceNeboKatastralniUzemi")
            val castObceNeboKatastralniUzemi: String? = null,
            @SerializedName("PSC")
            val psc: String? = null,
            @SerializedName("KrajNazev")
            val krajNazev: String? = null,
        ) {
            override fun toString(): String {
                return (uliceNazev ?: "") + " " + (cisloDomovni ?: "") + ", " + (psc ?: "") + " " + (obecNazev ?: "")
            }
        }

        data class Email(
            @SerializedName("Polozka")
            val polozka: Any? = null
        ) {
            data class Polozka(
                @SerializedName("Email")
                val email: String = "",
                @SerializedName("Poznamka")
                val poznamka: String? = null,
                @SerializedName("Typ")
                val typ: String? = null
            )
        }
    }

    data class FinanciUrad(
        val id: String = "",

        val type: String? = null,
        val subtype: Int? = null,

        val name: Name? = null,
        val ico: String? = null,
        val address: Address? = null,

        val pdz: Boolean? = null,
        val ovm: Boolean? = null,
        val hierarchy: Hierarchy? = null,
        @SerializedName("idOVM")
        val idOvm: String? = null
    ) {
        data class Name(
            val person: Person? = null,
            val tradeName: String? = null
        ) {
            data class Person(
                val firstName: String? = null,
                val lastName: String? = null
            )
        }

        data class Address(
            val addressPoint: Int = 0,
            val city: String? = null,
            val district: String? = null,
            val street: String? = null,
            val cp: Int? = null,
            val co: Int? = null,
            val ce: Int? = null,
            val zip: Int? = null,
            val state: String? = null,
            val fullAddress: String? = null
        ) {
            override fun toString(): String {
                return (street ?: "") + " " + (cp ?: "") + ", " + (zip ?: "") + " " + (city ?: "")
            }
        }

        data class Hierarchy(
            val isMaster: Boolean = true,
            val masterId: String? = null
        )
    }

    data class Soudy(
        val krajskySoud: OfficeResponse.Urad? = null,
        val okresniSoud: OfficeResponse.Urad? = null,
        val vrchniSoud: Urad? = null
    )

    fun toOffices(): List<Office?> {
        return listOf(
            if(okresniSoudApiTalksId != null)
            Office(
                okresniSoudApiTalksId,
                OfficeType.DistrictCourt,
                soudy?.okresniSoud?.nazev ?: "",
                LatLng(0.0,0.0),
                soudy?.okresniSoud?.adresaUradu?.toString() ?: "",
                OfficeInfo(null, null, null)
            ) else null,
            if(krajskySoudApiTalksId != null)
            Office(
                krajskySoudApiTalksId,
                OfficeType.RegionalCourt,
                soudy?.krajskySoud?.nazev ?: "",
                LatLng(0.0, 0.0),
                soudy?.krajskySoud?.adresaUradu?.toString() ?: "",
                OfficeInfo(null, null, null)
            ) else null,
            if(vrchniSoudApiTalksId != null)
            Office(
                vrchniSoudApiTalksId,
                OfficeType.HighCourt,
                soudy?.vrchniSoud?.nazev ?: "",
                LatLng(0.0, 0.0),
                soudy?.vrchniSoud?.adresaUradu?.toString() ?: "",
                OfficeInfo(null, null, null)
            ) else null,
            if(celniSpravaApiTalksId != null)
            Office(
                 celniSpravaApiTalksId,
                OfficeType.CustomsOffice,
                celniSprava?.nazev ?: "",
                LatLng(0.0, 0.0),
                celniSprava?.adresaUradu?.toString() ?: "",
                OfficeInfo(null, null, null)
            ) else null,
            if (financniUradApiTalksId != null)
            Office(
                financniUradApiTalksId,
                OfficeType.TaxOffice,
                financiUrad?.name?.tradeName ?: "",
                LatLng(0.0, 0.0),
                financiUrad?.address?.toString() ?: "",
                OfficeInfo(null, null, null)
            ) else null,
            if( csszApiTalksId != null)
            Office(
                csszApiTalksId,
                OfficeType.LabourOffice,
                cssz?.nazev ?: "",
                LatLng(0.0, 0.0),
                cssz?.adresaUradu?.toString() ?: "",
                OfficeInfo(null, null, null)
            ) else null,
            if (obecniUradApiTalksId != null)
            Office(
                obecniUradApiTalksId,
                OfficeType.CityGovernmentOffice,
                obecniUrad?.nazev ?: "",
                LatLng(0.0, 0.0),
                obecniUrad?.adresaUradu?.toString() ?: "",
                OfficeInfo(null, null, null)
            ) else null
        )
    }
}