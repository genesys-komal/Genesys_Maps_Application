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
 * @param `value` The text of the term.
 * @param offset Defines the start position of this term in the description, measured in Unicode characters
 */


data class PlaceAutocompleteTerm (

    /* The text of the term. */
    @SerializedName("value")
    val `value`: kotlin.String,

    /* Defines the start position of this term in the description, measured in Unicode characters */
    @SerializedName("offset")
    val offset: java.math.BigDecimal

)

