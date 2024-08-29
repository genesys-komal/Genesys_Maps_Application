package com.example.mapapplication.originCode.net.api

import com.example.mapapplication.originCode.net.models.IsochroneRequest
import com.example.mapapplication.originCode.net.models.IsochroneResponse
import com.example.mapapplication.originCode.net.models.LocateObject
import com.example.mapapplication.originCode.net.models.MatrixRequest
import com.example.mapapplication.originCode.net.models.MatrixResponse
import com.example.mapapplication.originCode.net.models.NearestRoadsRequest
import com.example.mapapplication.originCode.net.models.RouteOsrmResponse
import com.example.mapapplication.originCode.net.models.RouteRequest
import com.example.mapapplication.originCode.net.models.RouteResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface DirectionsApi {

    /**
     * Get turn by turn routing instructions between two or more locations.
     * The route (turn-by-turn) API computes routes between two or more locations. It supports a variety of tunable costing methods, and supports routing through intermediate waypoints and discontinuous multi-leg routes. more at https://github.com/aplog1c/origins-directions-api-spec/docs/turn-by-turn.md
     * Responses:
     *  - 200: Returns the computed route
     *  - 0: Unexpected error
     *
     * @param routeRequest  (optional)
     * @return [RouteResponse]
     */
    @POST("directions/route")
    suspend fun getDirectionsRoute(@Body routeRequest: RouteRequest? = null): Response<RouteResponse>

    /**
     * Get turn by turn routing instructions between two or more locations.
     * The route (turn-by-turn) API computes routes between two or more locations. It supports a variety of tunable costing methods, and supports routing through intermediate waypoints and discontinuous multi-leg routes. more at https://github.com/aplog1c/origins-directions-api-spec/docs/turn-by-turn.md
     * Responses:
     *  - 200: Returns the computed route
     *  - 0: Unexpected error
     *
     * 	@param format: Four options are available:
     * json is default valhalla routing directions JSON format
     * gpx returns the route as a GPX (GPS exchange format) XML track
     * osrm creates a OSRM compatible route directions JSON
     * pbf formats the result using protocol buffers
     *
     * @param routeRequest  (optional)
     * @return [RouteResponse]
     */
    @POST("directions/route")
    suspend fun getDirectionsRouteOsrm(
        @Query("format") format: String = "osrm", @Body routeRequest: RouteRequest? = null
    ): Response<RouteOsrmResponse>

    /**
     * Find the nearest roads to the set of input locations.
     * The nearest roads API allows you query for detailed information about streets and intersections near the input locations.
     * Responses:
     *  - 200: Returns a list of streets and intersections that match the query.
     *  - 0: Unexpected error
     *
     * @param nearestRoadsRequest  (optional)
     * @return [kotlin.collections.List<LocateObject>]
     */
    @POST("directions/locate")
    suspend fun getDirectionsLocate(@Body nearestRoadsRequest: NearestRoadsRequest? = null): Response<List<LocateObject>>

    /**
     * Calculate a time distance matrix for a grid of start and end points.
     * The time distance matrix API lets you compare travel times between a set of possible start and end points. Note that all matrix endpoints have a limit of 1000 elements, regardless of the costing/mode of travel. A matrix request with 3 inputs and 5 outputs has 3 x 5 &#x3D; 15 elements. This means you could send a 100 x 10 or 20 x 50 matrix request (each having 1000 elements), but not 40 x 30 as it has 1200 elements.
     * Responses:
     *  - 200: Returns a matrix of times and distances between the start and end points.
     *  - 0: Unexpected error
     *
     * @param matrixRequest  (optional)
     * @return [MatrixResponse]
     */
    @POST("directions/sources_to_targets")
    suspend fun getDirectionsDistanceMatrix(@Body matrixRequest: MatrixRequest? = null): Response<MatrixResponse>


    /**
     * Get isochrone implementation from centre point.
     * it highlight the are as per request data provided
     * Responses:
     *  - 200: Returns the computed isochrone
     *  - 0: Unexpected error
     *
     * @param isochroneRequest  (optional)
     * @return [IsochroneResponse]
     */
    @POST("directions/isochrone")
    suspend fun getDirectionsIsochrone(@Body isochroneRequest: IsochroneRequest? = null): Response<IsochroneResponse>
}
