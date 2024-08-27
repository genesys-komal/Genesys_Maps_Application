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
 * @param attribution A URL containing attribution information. If you are not using Stadia Maps and our standard attribution already for your basemaps, you must include this attribution link somewhere in your website/app.
 * @param query Technical details of the query. This is most useful for debugging during development. See the full example for the list of properties; these should be self-explanatory, so we don't enumerate them in the spec.
 * @param warnings An array of non-critical warnings. This is normally for informational/debugging purposes and not a serious problem.
 * @param errors An array of more serious errors (for example, omitting a required parameter). Don’t ignore these.
 */


data class GeocodingObject (

    /* A URL containing attribution information. If you are not using Stadia Maps and our standard attribution already for your basemaps, you must include this attribution link somewhere in your website/app. */
    @SerializedName("attribution")
    val attribution: java.net.URI? = null,

    /* Technical details of the query. This is most useful for debugging during development. See the full example for the list of properties; these should be self-explanatory, so we don't enumerate them in the spec. */
    @SerializedName("query")
    val query: kotlin.collections.Map<kotlin.String, kotlin.Any>? = null,

    /* An array of non-critical warnings. This is normally for informational/debugging purposes and not a serious problem. */
    @SerializedName("warnings")
    val warnings: kotlin.collections.List<kotlin.String>? = null,

    /* An array of more serious errors (for example, omitting a required parameter). Don’t ignore these. */
    @SerializedName("errors")
    val errors: kotlin.collections.List<kotlin.String>? = null

)

