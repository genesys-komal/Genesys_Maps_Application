package com.example.mapapplication.originCode.net.models

import com.google.gson.annotations.SerializedName

data class RouteOsrmResponse(
    @SerializedName("routes") val routes: List<Route>,
    @SerializedName("waypoints") val waypoints: List<Waypoint>,
    @SerializedName("code") val code: String
)

data class Route(
    @SerializedName("weight_name") val weightName: String,
    @SerializedName("weight") val weight: Double,
    @SerializedName("duration") val duration: Double,
    @SerializedName("distance") val distance: Double,
    @SerializedName("legs") val legs: List<Leg>,
    @SerializedName("geometry") val geometry: String
)

data class Leg(
    @SerializedName("via_waypoints") val viaWaypoints: List<Any>,
    @SerializedName("admins") val admins: List<Any>,
    @SerializedName("weight") val weight: Double,
    @SerializedName("duration") val duration: Double,
    @SerializedName("steps") val steps: List<Step>,
    @SerializedName("distance") val distance: Double,
    @SerializedName("summary") val summary: String
)

data class Step(
    @SerializedName("intersections") val intersections: List<Intersection>,
    @SerializedName("maneuver") val maneuver: Maneuver,
    @SerializedName("name") val name: String,
    @SerializedName("duration") val duration: Double,
    @SerializedName("distance") val distance: Double,
    @SerializedName("driving_side") val drivingSide: String,
    @SerializedName("weight") val weight: Double,
    @SerializedName("mode") val mode: String,
    @SerializedName("ref") val ref: String?,
    @SerializedName("geometry") val geometry: String,
    @SerializedName("bannerInstructions") val bannerInstructions: List<BannerInstruction>,
    @SerializedName("voiceInstructions") val voiceInstructions: List<VoiceInstruction>
)
data class BannerInstruction(
    @SerializedName("primary") val primary: Primary,
    @SerializedName("distanceAlongGeometry") val distanceAlongGeometry: Double
)

data class Primary(
    @SerializedName("type") val type: String,
    @SerializedName("modifier") val modifier: String,
    @SerializedName("text") val text: String,
    @SerializedName("components") val components: List<Component>
)

data class Component(
    @SerializedName("text") val text: String,
    @SerializedName("type") val type: String
)

data class VoiceInstruction(
    @SerializedName("announcement") val announcement: String,
    @SerializedName("distanceAlongGeometry") val distanceAlongGeometry: Double
)

data class Intersection(
    @SerializedName("entry") val entry: List<Boolean>,
    @SerializedName("bearings") val bearings: List<Int>,
    @SerializedName("duration") val duration: Double,
    @SerializedName("turn_weight") val turnWeight: Double?,
    @SerializedName("turn_duration") val turnDuration: Double?,
    @SerializedName("admin_index") val adminIndex: Int,
    @SerializedName("out") val out: Int?,
    @SerializedName("weight") val weight: Double,
    @SerializedName("geometry_index") val geometryIndex: Int,
    @SerializedName("location") val location: List<Double>,
    @SerializedName("in_index") val inIndex: Int?
)

data class Maneuver(
    @SerializedName("modifier") val modifier: String?,
    @SerializedName("instruction") val instruction: String,
    @SerializedName("type") val type: String,
    @SerializedName("bearing_after") val bearingAfter: Int,
    @SerializedName("bearing_before") val bearingBefore: Int,
    @SerializedName("location") val location: List<Double>
)

data class Waypoint(
    @SerializedName("distance") val distance: Double,
    @SerializedName("name") val name: String,
    @SerializedName("location") val location: List<Double>
)