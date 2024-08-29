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
 * @param distance The distance (in `units`) between the location in `sources` at `from_index` and the location in `targets` at `to_index`.
 * @param time The travel time (in seconds) between the location in `sources` at `from_index` and the location in `targets` at `to_index`.
 * @param fromIndex The index of the start location in the `sources` array.
 * @param toIndex The index of the end location in the `targets` array.
 */


data class MatrixDistance (

    /* The distance (in `units`) between the location in `sources` at `from_index` and the location in `targets` at `to_index`. */
    @SerializedName("distance")
    val distance: kotlin.Double,

    /* The travel time (in seconds) between the location in `sources` at `from_index` and the location in `targets` at `to_index`. */
    @SerializedName("time")
    val time: kotlin.Int,

    /* The index of the start location in the `sources` array. */
    @SerializedName("from_index")
    val fromIndex: kotlin.Int,

    /* The index of the end location in the `targets` array. */
    @SerializedName("to_index")
    val toIndex: kotlin.Int

)

