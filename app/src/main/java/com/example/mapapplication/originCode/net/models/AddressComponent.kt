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
 * @param longName The full text description or name of the address component as returned by the Geocoder.
 * @param shortName An abbreviated textual name for the address component, if available. For example, an address component for the state of Alaska may have a long_name of \"Alaska\" and a short_name of \"AK\" using the 2-letter postal abbreviation.
 * @param types An array indicating the type of the address component. See the list of [supported types](https://developers.origins.com/maps/documentation/places/web-service/supported_types).
 */


data class AddressComponent (

    /* The full text description or name of the address component as returned by the Geocoder. */
    @SerializedName("long_name")
    val longName: kotlin.String,

    /* An abbreviated textual name for the address component, if available. For example, an address component for the state of Alaska may have a long_name of \"Alaska\" and a short_name of \"AK\" using the 2-letter postal abbreviation. */
    @SerializedName("short_name")
    val shortName: kotlin.String,

    /* An array indicating the type of the address component. See the list of [supported types](https://developers.origins.com/maps/documentation/places/web-service/supported_types). */
    @SerializedName("types")
    val types: kotlin.collections.List<kotlin.String>

)

