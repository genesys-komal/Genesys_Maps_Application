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
 * Status codes returned by service. - `OK` indicating the API request was successful. - `ZERO_RESULTS` indicating that the search was successful but returned no results. This may occur if the search was passed a `latlng` in a remote location. - `INVALID_REQUEST` indicating the API request was malformed, generally due to missing required query parameter (`location` or `radius`). - `OVER_QUERY_LIMIT` indicating any of the following:   - You have exceeded the QPS limits.   - Billing has not been enabled on your account.   - The monthly $200 credit, or a self-imposed usage cap, has been exceeded.   - The provided method of payment is no longer valid (for example, a credit card has expired).   See the [Maps FAQ](https://developers.origins.com/maps/faq#over-limit-key-error) for more information about how to resolve this error. - `REQUEST_DENIED` indicating that your request was denied, generally because:   - The request is missing an API key.   - The `key` parameter is invalid. - `UNKNOWN_ERROR` indicating an unknown error. 
 *
 * Values: OK,ZERO_RESULTS,INVALID_REQUEST,OVER_QUERY_LIMIT,REQUEST_DENIED,UNKNOWN_ERROR
 */

enum class PlacesSearchStatus(val value: kotlin.String) {

    @SerializedName(value = "OK")
    OK("OK"),

    @SerializedName(value = "ZERO_RESULTS")
    ZERO_RESULTS("ZERO_RESULTS"),

    @SerializedName(value = "INVALID_REQUEST")
    INVALID_REQUEST("INVALID_REQUEST"),

    @SerializedName(value = "OVER_QUERY_LIMIT")
    OVER_QUERY_LIMIT("OVER_QUERY_LIMIT"),

    @SerializedName(value = "REQUEST_DENIED")
    REQUEST_DENIED("REQUEST_DENIED"),

    @SerializedName(value = "UNKNOWN_ERROR")
    UNKNOWN_ERROR("UNKNOWN_ERROR");

    /**
     * Override [toString()] to avoid using the enum variable name as the value, and instead use
     * the actual value defined in the API spec file.
     *
     * This solves a problem when the variable name and its value are different, and ensures that
     * the client sends the correct enum values to the server always.
     */
    override fun toString(): kotlin.String = value

    companion object {
        /**
         * Converts the provided [data] to a [String] on success, null otherwise.
         */
        fun encode(data: kotlin.Any?): kotlin.String? = if (data is PlacesSearchStatus) "$data" else null

        /**
         * Returns a valid [PlacesSearchStatus] for [data], null otherwise.
         */
        fun decode(data: kotlin.Any?): PlacesSearchStatus? = data?.let {
          val normalizedData = "$it".lowercase()
          values().firstOrNull { value ->
            it == value || normalizedData == "$value".lowercase()
          }
        }
    }
}

