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
 * 
 *
 * @param day A number from 0–6, corresponding to the days of the week, starting on Sunday. For example, 2 means Tuesday.
 * @param time May contain a time of day in 24-hour hhmm format. Values are in the range 0000–2359. The time will be reported in the place’s time zone.
 */


data class PlaceOpeningHoursPeriodDetail (

    /* A number from 0–6, corresponding to the days of the week, starting on Sunday. For example, 2 means Tuesday. */
    @SerializedName("day")
    val day: java.math.BigDecimal,

    /* May contain a time of day in 24-hour hhmm format. Values are in the range 0000–2359. The time will be reported in the place’s time zone. */
    @SerializedName("time")
    val time: kotlin.String

)

