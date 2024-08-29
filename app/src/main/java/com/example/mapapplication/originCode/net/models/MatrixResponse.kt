package com.example.mapapplication.originCode.net.models

import com.google.gson.annotations.SerializedName

/**
 * 
 *
 * @param sources The list of starting locations
 * @param targets The list of ending locations
 * @param sourcesToTargets The matrix of starting and ending locations, along with the computed distance and travel time. The array is row-ordered. This means that the time and distance from the first location to all others forms the first row of the array, followed by the time and distance from the second source location to all target locations, etc.
 * @param units 
 * @param id An identifier to disambiguate requests (echoed by the server).
 * @param warnings 
 */


data class MatrixResponse (

    /* The list of starting locations */
    @SerializedName("sources")
    val sources: List<Coordinate>,

    /* The list of ending locations */
    @SerializedName("targets")
    val targets: List<Coordinate>,

    /* The matrix of starting and ending locations, along with the computed distance and travel time. The array is row-ordered. This means that the time and distance from the first location to all others forms the first row of the array, followed by the time and distance from the second source location to all target locations, etc. */
    @SerializedName("sources_to_targets")
    val sourcesToTargets: List<List<MatrixDistance>>,

    @SerializedName("units")
    val units: ValhallaLongUnits = ValhallaLongUnits.kilometers,

    /* An identifier to disambiguate requests (echoed by the server). */
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("warnings")
    val warnings: List<Warning>? = null

)

