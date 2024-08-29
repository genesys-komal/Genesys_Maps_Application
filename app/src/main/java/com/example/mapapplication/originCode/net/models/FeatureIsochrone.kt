
@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport"
)
package com.example.mapapplication.originCode.net.models

import com.google.gson.annotations.SerializedName

data class FeatureIsochrone(
    @SerializedName("properties")
    val properties : PropertiesIsochrone ,

    @SerializedName("geometry")
    val geometry: Geometry ,

    @SerializedName("type")
    val type :String
)
