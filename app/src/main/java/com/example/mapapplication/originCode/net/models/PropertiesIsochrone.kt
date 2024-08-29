package com.example.mapapplication.originCode.net.models

import com.google.gson.annotations.SerializedName

data class PropertiesIsochrone(

    @SerializedName("fill-opacity")
    val fillOpacity: Double,
    @SerializedName("fillColor")
    val fillColor: String,
    @SerializedName("opacity")
    val opacity: Double,
    @SerializedName("fill")
    val fill: String,
    @SerializedName("color")
    val color: String,
    @SerializedName("contour")
    val contour: Integer,
    @SerializedName("metric")
    val metric: String
)
