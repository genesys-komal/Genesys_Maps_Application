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
 * @param lat The latitude of a point in the shape.
 * @param lon The longitude of a point in the shape.
 * @param type A `break` represents the start or end of a leg, and allows reversals. A `through` location is an intermediate waypoint that must be visited between `break`s, but at which reversals are not allowed. A `via` is similar to a `through` except that reversals are allowed. A `break_through` is similar to a `break` in that it can be the start/end of a leg, but does not allow reversals.
 * @param heading The preferred direction of travel when starting the route, in integer clockwise degrees from north. North is 0, south is 180, east is 90, and west is 270.
 * @param headingTolerance The tolerance (in degrees) determining whether a street is considered the same direction.
 * @param minimumReachability The minimum number of nodes that must be reachable for a given edge to consider that edge as belonging to a connected region. If a candidate edge has fewer connections, it will be considered a disconnected island.
 * @param radius The distance (in meters) to look for candidate edges around the location for purposes of snapping locations to the route graph. If there are no candidates within this distance, the closest candidate within a reasonable search distance will be used. This is subject to clamping by internal limits.
 * @param rankCandidates If true, candidates will be ranked according to their distance from the target location as well as other factors. If false, candidates will only be ranked using their distance from the target.
 * @param preferredSide If the location is not offset from the road centerline or is closest to an intersection, this option has no effect. Otherwise, the preferred side of street is used to determine whether or not the location should be visited from the same, opposite or either side of the road with respect to the side of the road the given locale drives on.
 * @param nodeSnapTolerance During edge correlation this is the tolerance (in meters) used to determine whether or not to snap to the intersection rather than along the street, if the snap location is within this distance from the intersection, the intersection is used instead.
 * @param streetSideTolerance A tolerance in meters from the edge centerline used for determining the side of the street that the location is on. If the distance to the centerline is less than this tolerance, no side will be inferred. Otherwise, the left or right side will be selected depending on the direction of travel.
 * @param streetSideMaxDistance A tolerance in meters from the edge centerline used for determining the side of the street that the location is on. If the distance to the centerline is greater than this tolerance, no side will be inferred. Otherwise, the left or right side will be selected depending on the direction of travel.
 * @param searchFilter 
 */


data class RoutingWaypoint (

    /* The latitude of a point in the shape. */
    @SerializedName("lat")
    val lat: kotlin.Double,

    /* The longitude of a point in the shape. */
    @SerializedName("lon")
    val lon: kotlin.Double,

    /* A `break` represents the start or end of a leg, and allows reversals. A `through` location is an intermediate waypoint that must be visited between `break`s, but at which reversals are not allowed. A `via` is similar to a `through` except that reversals are allowed. A `break_through` is similar to a `break` in that it can be the start/end of a leg, but does not allow reversals. */
    @SerializedName("type")
    val type: Type? = Type.`break`,

    /* The preferred direction of travel when starting the route, in integer clockwise degrees from north. North is 0, south is 180, east is 90, and west is 270. */
    @SerializedName("heading")
    val heading: kotlin.Int? = null,

    /* The tolerance (in degrees) determining whether a street is considered the same direction. */
    @SerializedName("heading_tolerance")
    val headingTolerance: kotlin.Int? = 60,

    /* The minimum number of nodes that must be reachable for a given edge to consider that edge as belonging to a connected region. If a candidate edge has fewer connections, it will be considered a disconnected island. */
    @SerializedName("minimum_reachability")
    val minimumReachability: kotlin.Int? = 50,

    /* The distance (in meters) to look for candidate edges around the location for purposes of snapping locations to the route graph. If there are no candidates within this distance, the closest candidate within a reasonable search distance will be used. This is subject to clamping by internal limits. */
    @SerializedName("radius")
    val radius: kotlin.Int? = 0,

    /* If true, candidates will be ranked according to their distance from the target location as well as other factors. If false, candidates will only be ranked using their distance from the target. */
    @SerializedName("rank_candidates")
    val rankCandidates: kotlin.Boolean? = true,

    /* If the location is not offset from the road centerline or is closest to an intersection, this option has no effect. Otherwise, the preferred side of street is used to determine whether or not the location should be visited from the same, opposite or either side of the road with respect to the side of the road the given locale drives on. */
    @SerializedName("preferred_side")
    val preferredSide: PreferredSide? = null,

    /* During edge correlation this is the tolerance (in meters) used to determine whether or not to snap to the intersection rather than along the street, if the snap location is within this distance from the intersection, the intersection is used instead. */
    @SerializedName("node_snap_tolerance")
    val nodeSnapTolerance: kotlin.Int? = 5,

    /* A tolerance in meters from the edge centerline used for determining the side of the street that the location is on. If the distance to the centerline is less than this tolerance, no side will be inferred. Otherwise, the left or right side will be selected depending on the direction of travel. */
    @SerializedName("street_side_tolerance")
    val streetSideTolerance: kotlin.Int? = 5,

    /* A tolerance in meters from the edge centerline used for determining the side of the street that the location is on. If the distance to the centerline is greater than this tolerance, no side will be inferred. Otherwise, the left or right side will be selected depending on the direction of travel. */
    @SerializedName("street_side_max_distance")
    val streetSideMaxDistance: kotlin.Int? = 1000,

    @SerializedName("search_filter")
    val searchFilter: RoutingWaypointAllOfSearchFilter? = null

) {

    /**
     * A `break` represents the start or end of a leg, and allows reversals. A `through` location is an intermediate waypoint that must be visited between `break`s, but at which reversals are not allowed. A `via` is similar to a `through` except that reversals are allowed. A `break_through` is similar to a `break` in that it can be the start/end of a leg, but does not allow reversals.
     *
     * Values: `break`,through,via,break_through
     */
    enum class Type(val value: kotlin.String) {
        @SerializedName(value = "break") `break`("break"),
        @SerializedName(value = "through") through("through"),
        @SerializedName(value = "via") via("via"),
        @SerializedName(value = "break_through") break_through("break_through");
    }
    /**
     * If the location is not offset from the road centerline or is closest to an intersection, this option has no effect. Otherwise, the preferred side of street is used to determine whether or not the location should be visited from the same, opposite or either side of the road with respect to the side of the road the given locale drives on.
     *
     * Values: same,opposite,either
     */
    enum class PreferredSide(val value: kotlin.String) {
        @SerializedName(value = "same") same("same"),
        @SerializedName(value = "opposite") opposite("opposite"),
        @SerializedName(value = "either") either("either");
    }
}

