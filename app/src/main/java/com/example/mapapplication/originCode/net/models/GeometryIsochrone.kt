package com.example.mapapplication.originCode.net.models

import com.google.gson.annotations.SerializedName

data class GeometryIsochrone(
    @SerializedName("coordinates")
    val coordinates: List<List<List<Double>>>,
    @SerializedName("type")
    val type: String
)
