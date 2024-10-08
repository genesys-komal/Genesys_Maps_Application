/**
 *
 * Please note:
 * This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 *
 */

@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport"
)

package com.example.mapapplication.originCode.net.models


import com.google.gson.annotations.SerializedName

/**
 * Attributes used to describe a cell tower. The following optional fields are not currently used, but may be included if values are available: `age`, `signalStrength`, `timingAdvance`.
 *
 * @param cellId Unique identifier of the cell. On GSM, this is the Cell ID (CID); CDMA networks use the Base Station ID (BID). WCDMA networks use the UTRAN/GERAN Cell Identity (UC-Id), which is a 32-bit value concatenating the Radio Network Controller (RNC) and Cell ID. Specifying only the 16-bit Cell ID value in WCDMA networks may return inaccurate results.
 * @param locationAreaCode The Location Area Code (LAC) for GSM and WCDMA networks. The Network ID (NID) for CDMA networks.
 * @param mobileCountryCode The cell tower's Mobile Country Code (MCC).
 * @param mobileNetworkCode The cell tower's Mobile Network Code. This is the MNC for GSM and WCDMA; CDMA uses the System ID (SID).
 * @param age The number of milliseconds since this cell was primary. If age is 0, the cellId represents a current measurement.
 * @param signalStrength Radio signal strength measured in dBm.
 * @param timingAdvance The timing advance value.
 */


data class CellTower (

    /* Unique identifier of the cell. On GSM, this is the Cell ID (CID); CDMA networks use the Base Station ID (BID). WCDMA networks use the UTRAN/GERAN Cell Identity (UC-Id), which is a 32-bit value concatenating the Radio Network Controller (RNC) and Cell ID. Specifying only the 16-bit Cell ID value in WCDMA networks may return inaccurate results. */
    @SerializedName("cellId")
    val cellId: kotlin.Int,

    /* The Location Area Code (LAC) for GSM and WCDMA networks. The Network ID (NID) for CDMA networks. */
    @SerializedName("locationAreaCode")
    val locationAreaCode: kotlin.Int,

    /* The cell tower's Mobile Country Code (MCC). */
    @SerializedName("mobileCountryCode")
    val mobileCountryCode: kotlin.Int,

    /* The cell tower's Mobile Network Code. This is the MNC for GSM and WCDMA; CDMA uses the System ID (SID). */
    @SerializedName("mobileNetworkCode")
    val mobileNetworkCode: kotlin.Int,

    /* The number of milliseconds since this cell was primary. If age is 0, the cellId represents a current measurement. */
    @SerializedName("age")
    val age: kotlin.Int? = null,

    /* Radio signal strength measured in dBm. */
    @SerializedName("signalStrength")
    val signalStrength: java.math.BigDecimal? = null,

    /* The timing advance value. */
    @SerializedName("timingAdvance")
    val timingAdvance: java.math.BigDecimal? = null

)

