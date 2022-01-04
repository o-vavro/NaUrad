package com.atlasstudio.naurad.net.model


import com.google.gson.annotations.SerializedName

data class OfficeIdResponse(
    val data: List<Data> = listOf(),
    val skip: Int? = null,
    val count: Int? = null,
    val limit: Int? = null,
    val info: Info? = null
) {
    data class Data(
        val id: String = "",

        @SerializedName("KOD_ADM")
        val kodAdm: String? = null,
        @SerializedName("KOD_OBCE")
        val kodObce: String? = null,
        @SerializedName("KOD_MOMC")
        val kodMomc: String? = null,
        @SerializedName("KOD_MOP")
        val kodMop: String? = null,
        @SerializedName("KOD_CASTI_OBCE")
        val kodCastiObce: String? = null,
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

        @SerializedName("PLATI_OD")
        val platiOd: String? = null,

        val spadovost: Spadovost? = null
    ) {
        data class Spadovost(
            val id: String = "",
            @SerializedName("ZUJ")
            val zuj: String? = null,

            @SerializedName("KOD_MOMC")
            val kodMomc: String? = null,
            @SerializedName("KOD_CASTI_OBCE")
            val kodCastiObce: String? = null,

            @SerializedName("OKRESNI_SOUD_APITALKS_ID")
            val okresniSoudApiTalksId: String? = null,
            @SerializedName("KRAJSKY_SOUD_APITALKS_ID")
            val krajskySoudApiTalksId: String? = null,
            @SerializedName("VRCHNI_SOUD_APITALKS_ID")
            val vrchniSoudApiTalksId: String? = null,
            @SerializedName("CELNI_SPRAVA_APITALKS_ID")
            val celniSpravaApiTalksId: String? = null,
            @SerializedName("FINANCNI_URAD_APITALKS_ID")
            val financniUradApiTalksIdD: String? = null,
            @SerializedName("CSSZ_APITALKS_ID")
            val csszApiTalksId: String? = null,
            @SerializedName("URAD_APITALKS_ID")
            val obecniUradApiTalksId: String? = null
        )
    }

    data class Info(
        val provider: String? = null
    )
}