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
 * @param website 
 * @param wikipedia 
 * @param wikidata 
 * @param phone 
 */


data class PeliasGeoJSONPropertiesAddendumOsm (

    @SerializedName("website")
    val website: java.net.URI? = null,

    @SerializedName("wikipedia")
    val wikipedia: kotlin.String? = null,

    @SerializedName("wikidata")
    val wikidata: kotlin.String? = null,

    @SerializedName("phone")
    val phone: kotlin.String? = null

) : kotlin.collections.HashMap<String, kotlin.Any>()

