package com.example.mapapplication.originCode

import android.util.Log
import com.example.mapapplication.originCode.helpers.ioThread
import com.example.mapapplication.originCode.helpers.toOperationResult
import com.example.mapapplication.originCode.net.ApiClient
import com.example.mapapplication.originCode.net.api.DirectionsApi
import com.example.mapapplication.originCode.net.api.SearchApi
import com.example.mapapplication.originCode.net.api.SearchApi.LanguageGoogAutocomplete
import com.example.mapapplication.originCode.net.auth.ApiKeyAuth
import com.example.mapapplication.originCode.net.infrastructure.CollectionFormats.CSVParams
import com.example.mapapplication.originCode.net.models.Contour
import com.example.mapapplication.originCode.net.models.Coordinate
import com.example.mapapplication.originCode.net.models.CostingModel
import com.example.mapapplication.originCode.net.models.CostingOptions
import com.example.mapapplication.originCode.net.models.DirectionsOptions
import com.example.mapapplication.originCode.net.models.GoogPlacesAutocompleteResponse
import com.example.mapapplication.originCode.net.models.IsochroneCostingModel
import com.example.mapapplication.originCode.net.models.IsochroneRequest
import com.example.mapapplication.originCode.net.models.IsochroneResponse
import com.example.mapapplication.originCode.net.models.LocateObject
import com.example.mapapplication.originCode.net.models.MatrixCostingModel
import com.example.mapapplication.originCode.net.models.MatrixRequest
import com.example.mapapplication.originCode.net.models.MatrixResponse
import com.example.mapapplication.originCode.net.models.NearestRoadsRequest
import com.example.mapapplication.originCode.net.models.PeliasResponse
import com.example.mapapplication.originCode.net.models.RouteOsrmResponse
import com.example.mapapplication.originCode.net.models.RouteRequest
import com.example.mapapplication.originCode.net.models.RouteResponse
import com.example.mapapplication.originCode.net.models.RoutingWaypoint
import com.example.mapapplication.originCode.net.models.ValhallaLanguages

class GeoMaps {

    companion object {
        @Volatile
        private var instance: GeoMaps? = null
        private var apiKey: String = ""
        private var baseUrl: String = ""

        fun getInstance() = instance ?: synchronized(this) {
            instance ?: GeoMaps().also { instance = it }
        }

        fun close() {
            instance = null
        }
    }

    fun initialize(configuration: GeoMapsConfiguration.Builder) {
        apiKey = configuration.apiKey
        baseUrl = configuration.baseUrl
    }

    private fun getClient(): ApiClient {
        if (apiKey.isEmpty() || baseUrl.isEmpty()) {
            throw IllegalStateException("GeoMaps not initialized")
        }

        return ApiClient(baseUrl = baseUrl).apply {
            addAuthorization("apiKey", ApiKeyAuth("query", "api_key", apiKey))
        }
    }

    /**
     * Get turn by turn routing instructions between two or more locations.
     * The route (turn-by-turn) API computes routes between two or more locations.
     * It supports a variety of tunable costing methods, and supports routing through intermediate waypoints and discontinuous multi-leg routes.
     *
     * @param locations List of locations to route between.
     * @param costing Look in #CostingModel
     * @param id An identifier to disambiguate requests (echoed by the server).
     * @param costingOptions Look in #CostingOptions
     * @param avoidLocations Look in #RoutingWaypoint
     * @param avoidPolygons One or multiple exterior rings of polygons in the form of nested JSON arrays. Roads intersecting these rings will be avoided during path finding. Open rings will be closed automatically.
     * @param directionsOptions Look in #DirectionsOptions
     */
    fun getDirections(
        locations: List<RoutingWaypoint>,
        costing: CostingModel,
        id: String = "route",
        costingOptions: CostingOptions? = null,
        avoidLocations: List<RoutingWaypoint>? = null,
        avoidPolygons: List<List<List<Double>>>? = null,
        directionsOptions: DirectionsOptions? = null,
        onSuccess: (RouteResponse) -> Unit,
        onError: (String) -> Unit
    ) = ioThread {
        val service = getClient().createService(DirectionsApi::class.java)
        val routeRequest = RouteRequest(
            locations, costing, id, costingOptions, avoidLocations, avoidPolygons, directionsOptions
        )
        Log.d("getDirectionsRoute", routeRequest.toString())
        service.getDirectionsRoute(routeRequest).toOperationResult { it }
            .onSuccess {
                //Returns the computed route
                onSuccess(it)
            }
            .onError {
                //Unexpected error
                onError.invoke(it)
            }
    }

    /**
     * Get turn by turn routing instructions between two or more locations.
     * The route (turn-by-turn) API computes routes between two or more locations.
     * It supports a variety of tunable costing methods, and supports routing through intermediate waypoints and discontinuous multi-leg routes.
     *
     * @param locations List of locations to route between.
     * @param costing Look in #CostingModel
     * @param id An identifier to disambiguate requests (echoed by the server).
     * @param costingOptions Look in #CostingOptions
     * @param polygons If true, the generated GeoJSON will use polygons. The default is to use LineStrings. Polygon output makes it easier to render overlapping areas in some visualization tools (such as MapLibre renderers).
     * @param denoise A value in the range [0, 1] which will be used to smooth out or remove smaller contours. A value of 1 will only return the largest contour for a given time value. A value of 0.5 drops any contours that are less than half the area of the largest contour in the set of contours for that same time value.
     * @param contours Look in #Contours
     * @param show_locations If true, then the output GeoJSON will include the input locations as two MultiPoint features: one for the exact input coordinates, and a second for the route network node location that the point was snapped to.
     * @param generalize The value in meters to be used as a tolerance for Douglas-Peucker generalization.
     * @param avoidPolygons One or multiple exterior rings of polygons in the form of nested JSON arrays. Roads intersecting these rings will be avoided during path finding. Open rings will be closed automatically.
     * @param directionsOptions Look in #DirectionsOptions
     */
    fun getIsochrone(
        locations: List<RoutingWaypoint>,
        costing: IsochroneCostingModel,
        polygons: Boolean,
        denoise: Double,
        generalize: Double,
        show_locations: Boolean,
        contours: List<Contour>,
        costingOptions: CostingOptions?,
        directionsOptions: DirectionsOptions?,
        id: String,
        onSuccess: (IsochroneResponse) -> Unit,
        onError: (String) -> Unit
    ) = ioThread {
        val service = getClient().createService(DirectionsApi::class.java)
        val routeRequest = IsochroneRequest(
            locations,
            costing,
            contours,
            id,
            costingOptions,
            directionsOptions,
            polygons,
            denoise,
            generalize,
            show_locations
        )
        Log.d("isochroneRequestroute", routeRequest.toString())
        service.getDirectionsIsochrone(routeRequest).toOperationResult { it }
            .onSuccess {
                //Returns the computed route
                onSuccess(it)
            }
            .onError {
                //Unexpected error
                onError.invoke(it)
            }
    }

    /**
     * Get turn by turn routing instructions between two or more locations in osrm format.
     * The route (turn-by-turn) API computes routes between two or more locations.
     * It supports a variety of tunable costing methods, and supports routing through intermediate waypoints and discontinuous multi-leg routes.
     *
     * @param locations List of locations to route between.
     * @param costing Look in #CostingModel
     * @param id An identifier to disambiguate requests (echoed by the server).
     * @param costingOptions Look in #CostingOptions
     * @param avoidLocations Look in #RoutingWaypoint
     * @param avoidPolygons One or multiple exterior rings of polygons in the form of nested JSON arrays. Roads intersecting these rings will be avoided during path finding. Open rings will be closed automatically.
     * @param directionsOptions Look in #DirectionsOptions
     */
    fun getDirectionsOsrm(
        locations: List<RoutingWaypoint>,
        costing: CostingModel,
        id: String = "route",
        costingOptions: CostingOptions? = null,
        avoidLocations: List<RoutingWaypoint>? = null,
        avoidPolygons: List<List<List<Double>>>? = null,
        directionsOptions: DirectionsOptions? = null,
        bannerInstructions: Boolean = true,
        voiceInstructions: Boolean = true,
        language: ValhallaLanguages = ValhallaLanguages.enMinusUS,
        onSuccess: (RouteOsrmResponse) -> Unit,
        onError: (String) -> Unit
    ) = ioThread {
        val service = getClient().createService(DirectionsApi::class.java)
        val routeRequest = RouteRequest(
            locations, costing, id, costingOptions, avoidLocations, avoidPolygons,
            directionsOptions, bannerInstructions, voiceInstructions, language
        )

        service.getDirectionsRouteOsrm(routeRequest = routeRequest).toOperationResult { it }
            .onSuccess {
                //Returns the computed route
                onSuccess(it)
            }
            .onError {
                //Unexpected error
                onError.invoke(it)
            }
    }

    /**
     * Find the nearest roads to the set of input locations.
     * The nearest roads API allows you query for detailed information about streets and intersections near the input locations.
     *
     * @param locations Location list to find nearest roads for.
     * @param costing Look in #CostingModel
     * @param costingOptions Look in #CostingOptions
     * @param verbose If true, the response will include additional metadata about the nearest road.
     * @param directionsOptions Look in #DirectionsOptions
     */
    fun getNearestRoads(
        locations: List<Coordinate>,
        costing: CostingModel? = null,
        costingOptions: CostingOptions? = null,
        verbose: Boolean? = false,
        directionsOptions: DirectionsOptions? = null,
        onSuccess: (List<LocateObject>) -> Unit,
        onError: (String) -> Unit
    ) = ioThread {
        val service = getClient().createService(DirectionsApi::class.java)
        val nearestRoadsRequest = NearestRoadsRequest(
            locations, costing, costingOptions, verbose, directionsOptions
        )

        service.getDirectionsLocate(nearestRoadsRequest).toOperationResult { it }
            .onSuccess {
                // Returns a list of streets and intersections that match the query.
                onSuccess.invoke(it)
            }
            .onError {
                //Unexpected error
                onError.invoke(it)
            }
    }

    /**
     * Calculate a time distance matrix for a grid of start and end points.
     * The time distance matrix API lets you compare travel times between a set of possible start and end points.
     * Note that all matrix endpoints have a limit of 1000 elements, regardless of the costing/mode of travel.
     * A matrix request with 3 inputs and 5 outputs has 3 x 5 &#x3D; 15 elements.
     * This means you could send a 100 x 10 or 20 x 50 matrix request (each having 1000 elements), but not 40 x 30 as it has 1200 elements.
     *
     * @param sources The list of starting locations
     * @param targets The list of ending locations
     * @param costing Look in #MatrixCostingModel
     * @param id An identifier to disambiguate requests (echoed by the server).
     * @param costingOptions Look in #CostingOptions
     * @param matrixLocations Only applicable to one-to-many or many-to-one requests. This defaults to all locations. When specified explicitly, this option allows a partial result to be returned. This is basically equivalent to \"find the closest/best locations out of the full set. This can have a dramatic improvement for large requests.
     * @param directionsOptions Look in #DirectionsOptions
     */
    fun getTimeDistanceMatrix(
        sources: List<Coordinate>,
        targets: List<Coordinate>,
        costing: MatrixCostingModel,
        id: String = "matrix",
        costingOptions: CostingOptions? = null,
        matrixLocations: Int? = null,
        directionsOptions: DirectionsOptions? = null,
        onSuccess: (MatrixResponse) -> Unit,
        onError: (String) -> Unit
    ) = ioThread {
        val service = getClient().createService(DirectionsApi::class.java)
        val matrixRequest = MatrixRequest(
            sources, targets, costing, id, costingOptions, matrixLocations, directionsOptions
        )

        service.getDirectionsDistanceMatrix(matrixRequest).toOperationResult { it }
            .onSuccess {
                //Returns a matrix of times and distances between the start and end points.
                onSuccess.invoke(it)
            }
            .onError {
                //Unexpected error
                onError.invoke(it)
            }
    }

    /**
     * Autocomplete API as per google api spec
     * The Place Autocomplete service is a web service that returns place predictions in response to an HTTP request.
     * The request specifies a textual search string and optional geographic bounds.
     * The service can be used to provide autocomplete functionality for text-based geographic searches, by returning places such as businesses, addresses and points of interest as a user types.
     * The Place Autocomplete service can match on full words and substrings, resolving place names and addresses.
     * Applications can therefore send queries as the user types, to provide on-the-fly place predictions.
     * The returned predictions are designed to be presented to the user to aid them in selecting the desired place.
     * You can send a Place Details request for more information about any of the places which are returned.
     *
     *
     * @param input The text string on which to search. The Place Autocomplete service will return candidate matches based on this string and order results based on their perceived relevance.
     * @param focusPointLat The latitude of the point to focus the search on. This will bias results toward the focus point. This parameter is optional.
     * @param focusPointLon The longitude of the point to focus the search on. This will bias results toward the focus point. This parameter is optional.
     * @param boundaryRectMinLon Defines the min longitude component of a bounding box to limit the search to. Requires all other `boundary.rect` parameters to be specified. This parameter is optional.
     * @param boundaryRectMaxLon Defines the max longitude component of a bounding box to limit the search to. Requires all other `boundary.rect` parameters to be specified. This parameter is optional.
     * @param boundaryRectMinLat Defines the min latitude component of a bounding box to limit the search to. Requires all other `boundary.rect` parameters to be specified. This parameter is optional.
     * @param boundaryRectMaxLat Defines the max latitude component of a bounding box to limit the search to. Requires all other `boundary.rect` parameters to be specified. This parameter is optional.
     * @param boundaryCircleLat The latitude of the center of a circle to limit the search to. Requires `boundary.circle.lon`. This parameter is optional.
     * @param boundaryCircleLon The longitude of the center of a circle to limit the search to. Requires `boundary.circle.lat`. This parameter is optional.
     * @param boundaryCircleRadius The radius of the circle (in kilometers) to limit the search to. Defaults to 50km if unspecified. This parameter is optional.
     * @param boundaryCountry A list of countries to limit the search to. These may be either full names (ex: Canada), or an ISO 3116-1 alpha-2 or alpha-3 code. Prefer ISO codes when possible. This parameter is optional.
     * @param boundaryGid The Pelias GID of an area to limit the search to. This parameter is optional.
     * @param layers A list of layers to limit the search to. This parameter is optional.
     * @param sources A list of sources to limit the search to. This parameter is optional.
     * @param size The maximum number of results to return. This parameter is optional.
     * @param lang A BCP47 language tag which specifies a preference for localization of results. By default, results are in the default locale of the source data, but specifying a language will attempt to localize the results. Note that while a `langtag` (in RFC 5646 terms) can contain script, region, etc., only the `language` portion, an ISO 639 code, will be considered. So `en-US` and `en-GB` will both be treated as English. This parameter is optional and defaults to English.
     *
     */
    fun autoComplete(
        text: String,
        focusPointLat: Double? = null,
        focusPointLon: Double? = null,
        boundaryRectMinLat: Double? = null,
        boundaryRectMaxLat: Double? = null,
        boundaryRectMinLon: Double? = null,
        boundaryRectMaxLon: Double? = null,
        boundaryCircleLat: Double? = null,
        boundaryCircleLon: Double? = null,
        boundaryCircleRadius: Double? = null,
        boundaryCountry: List<String>? = null,
        boundaryGid: String? = null,
        layers: List<String>? = null,
        sources: List<String>? = null,
        size: Int? = null,
        language: LanguageGoogAutocomplete? = LanguageGoogAutocomplete.en,
        onSuccess: (GoogPlacesAutocompleteResponse) -> Unit,
        onError: (String) -> Unit
    ) = ioThread {
        val service = getClient().createService(SearchApi::class.java)
        service.googAutocomplete(
            text, focusPointLat, focusPointLon, boundaryRectMinLat, boundaryRectMaxLat,
            boundaryRectMinLon, boundaryRectMaxLon, boundaryCircleLat, boundaryCircleLon,
            boundaryCircleRadius, boundaryCountry, boundaryGid, layers, sources, size, language
        ).toOperationResult { it }
            .onSuccess {
                //Returns the collection of search results.
                onSuccess.invoke(it)
            }
            .onError {
                //Unexpected error
                onError.invoke(it)
            }
    }

    fun autoCompleteWithType(
        text: String,
        type: String,
        focusPointLat: Double? = null,
        focusPointLon: Double? = null,
        boundaryRectMinLat: Double? = null,
        boundaryRectMaxLat: Double? = null,
        boundaryRectMinLon: Double? = null,
        boundaryRectMaxLon: Double? = null,
        boundaryCircleLat: Double? = null,
        boundaryCircleLon: Double? = null,
        boundaryCircleRadius: Double? = null,
        boundaryCountry: List<String>? = null,
        boundaryGid: String? = null,
        layers: List<String>? = null,
        sources: List<String>? = null,
        size: Int? = null,
        language: LanguageGoogAutocomplete? = LanguageGoogAutocomplete.en,
        onSuccess: (GoogPlacesAutocompleteResponse) -> Unit,
        onError: (String) -> Unit
    ) = ioThread {
        val service = getClient().createService(SearchApi::class.java)
        service.googAutocompleteType(
            text, type, focusPointLat, focusPointLon, boundaryRectMinLat, boundaryRectMaxLat,
            boundaryRectMinLon, boundaryRectMaxLon, boundaryCircleLat, boundaryCircleLon,
            boundaryCircleRadius, boundaryCountry, boundaryGid, layers, sources, size, language
        ).toOperationResult { it }
            .onSuccess {
                //Returns the collection of search results.
                onSuccess.invoke(it)
            }
            .onError {
                //Unexpected error
                onError.invoke(it)
            }
    }

    /**
     * Retrieve details of a place using its GID.
     * Many search result components include an associated GID field (for example, an address may have a &#x60;localadmin_gid&#x60;). The place endpoint lets you look up these places directly by ID. Note that these IDs are not stable for all sources.
     *
     * @param ids A list of Pelias GIDs to search for.
     * @param lang A BCP47 language tag which specifies a preference for localization of results. By default, results are in the default locale of the source data, but specifying a language will attempt to localize the results. Note that while a &#x60;langtag&#x60; (in RFC 5646 terms) can contain script, region, etc., only the &#x60;language&#x60; portion, an ISO 639 code, will be considered. So &#x60;en-US&#x60; and &#x60;en-GB&#x60; will both be treated as English. (optional)
     */
    fun getPlaceDetails(
        ids: List<String>,
        lang: String? = null,
        onSuccess: (PeliasResponse) -> Unit,
        onError: (String) -> Unit
    ) = ioThread {
        val service = getClient().createService(SearchApi::class.java)
        service.getPlaceDetails(CSVParams(ids), lang).toOperationResult { it }
            .onSuccess {
                //Returns the collection of search results.
                onSuccess.invoke(it)
            }
            .onError {
                //Unexpected error
                onError.invoke(it)
            }
    }

    /**
     * Find places and addresses near geographic coordinates (reverse geocoding).
     * Reverse geocoding and search finds places and addresses near any geographic coordinates.
     *
     * @param pointLat The latitude of the point at which to perform the search.
     * @param pointLon The longitude of the point at which to perform the search.
     * @param boundaryCircleRadius The radius of the circle (in kilometers) to limit the search to. Defaults to 50km if unspecified. (optional)
     * @param layers A list of layers to limit the search to. (optional)
     * @param sources A list of sources to limit the search to. (optional)
     * @param boundaryCountry A list of countries to limit the search to. These may be either full names (ex: Canada), or an ISO 3116-1 alpha-2 or alpha-3 code. Prefer ISO codes when possible. (optional)
     * @param boundaryGid The Pelias GID of an area to limit the search to. (optional)
     * @param size The maximum number of results to return. (optional)
     * @param lang A BCP47 language tag which specifies a preference for localization of results. By default, results are in the default locale of the source data, but specifying a language will attempt to localize the results. Note that while a &#x60;langtag&#x60; (in RFC 5646 terms) can contain script, region, etc., only the &#x60;language&#x60; portion, an ISO 639 code, will be considered. So &#x60;en-US&#x60; and &#x60;en-GB&#x60; will both be treated as English. (optional)
     */
    fun getReverseGeocode(
        pointLat: Double,
        pointLon: Double,
        boundaryCircleRadius: Double? = null,
        layers: List<String>? = null,
        sources: List<String>? = null,
        boundaryCountry: List<String>? = null,
        boundaryGid: String? = null,
        size: Int? = null,
        lang: String? = null,
        onSuccess: (PeliasResponse) -> Unit,
        onError: (String) -> Unit
    ) = ioThread {
        val service = getClient().createService(SearchApi::class.java)
        val csvLayers = if (layers == null) null else CSVParams(layers)
        val csvSources = if (sources == null) null else CSVParams(sources)
        val csvBoundaryCountry = if (boundaryCountry == null) null else CSVParams(boundaryCountry)

        service.reverseGeocode(
            pointLat, pointLon,
            boundaryCircleRadius, csvLayers, csvSources, csvBoundaryCountry, boundaryGid, size, lang
        ).toOperationResult { it }
            .onSuccess {
                //Returns the collection of search results.
                onSuccess.invoke(it)
            }
            .onError {
                //Unexpected error
                onError.invoke(it)
            }
    }

    /**
     * Search for location and other info using a place name or address (forward geocoding).
     * The search endpoint lets you search for addresses, points of interest, and administrative areas.
     * This is most commonly used for forward geocoding applications where you need to find the geographic coordinates of an address.
     *
     * @param text The place name (address, venue name, etc.) to search for.
     * @param focusPointLat The latitude of the point to focus the search on. This will bias results toward the focus point. Requires &#x60;focus.point.lon&#x60;. (optional)
     * @param focusPointLon The longitude of the point to focus the search on. This will bias results toward the focus point. Requires &#x60;focus.point.lat&#x60;. (optional)
     * @param boundaryRectMinLat Defines the min latitude component of a bounding box to limit the search to. Requires all other &#x60;boundary.rect&#x60; parameters to be specified. (optional)
     * @param boundaryRectMaxLat Defines the max latitude component of a bounding box to limit the search to. Requires all other &#x60;boundary.rect&#x60; parameters to be specified. (optional)
     * @param boundaryRectMinLon Defines the min longitude component of a bounding box to limit the search to. Requires all other &#x60;boundary.rect&#x60; parameters to be specified. (optional)
     * @param boundaryRectMaxLon Defines the max longitude component of a bounding box to limit the search to. Requires all other &#x60;boundary.rect&#x60; parameters to be specified. (optional)
     * @param boundaryCircleLat The latitude of the center of a circle to limit the search to. Requires &#x60;boundary.circle.lon&#x60;. (optional)
     * @param boundaryCircleLon The longitude of the center of a circle to limit the search to. Requires &#x60;boundary.circle.lat&#x60;. (optional)
     * @param boundaryCircleRadius The radius of the circle (in kilometers) to limit the search to. Defaults to 50km if unspecified. (optional)
     * @param boundaryCountry A list of countries to limit the search to. These may be either full names (ex: Canada), or an ISO 3116-1 alpha-2 or alpha-3 code. Prefer ISO codes when possible. (optional)
     * @param boundaryGid The Pelias GID of an area to limit the search to. (optional)
     * @param layers A list of layers to limit the search to. (optional)
     * @param sources A list of sources to limit the search to. (optional)
     * @param size The maximum number of results to return. (optional)
     * @param lang A BCP47 language tag which specifies a preference for localization of results. By default, results are in the default locale of the source data, but specifying a language will attempt to localize the results. Note that while a &#x60;langtag&#x60; (in RFC 5646 terms) can contain script, region, etc., only the &#x60;language&#x60; portion, an ISO 639 code, will be considered. So &#x60;en-US&#x60; and &#x60;en-GB&#x60; will both be treated as English. (optional)
     */
    fun getForwardGeocode(
        text: String,
        focusPointLat: Double? = null,
        focusPointLon: Double? = null,
        boundaryRectMinLat: Double? = null,
        boundaryRectMaxLat: Double? = null,
        boundaryRectMinLon: Double? = null,
        boundaryRectMaxLon: Double? = null,
        boundaryCircleLat: Double? = null,
        boundaryCircleLon: Double? = null,
        boundaryCircleRadius: Double? = null,
        boundaryCountry: List<String>? = null,
        boundaryGid: String? = null,
        layers: List<String>? = null,
        sources: List<String>? = null,
        size: Int? = null,
        lang: String? = null,
        onSuccess: (PeliasResponse) -> Unit,
        onError: (String) -> Unit
    ) = ioThread {
        val service = getClient().createService(SearchApi::class.java)
        val csvBoundaryCountry = if (boundaryCountry == null) null else CSVParams(boundaryCountry)
        val csvLayers = if (layers == null) null else CSVParams(layers)
        val csvSources = if (sources == null) null else CSVParams(sources)

        service.forwardGeocode(
            text, focusPointLat, focusPointLon, boundaryRectMinLat, boundaryRectMaxLat,
            boundaryRectMinLon, boundaryRectMaxLon, boundaryCircleLat, boundaryCircleLon,
            boundaryCircleRadius, csvBoundaryCountry, boundaryGid, csvLayers, csvSources, size, lang
        ).toOperationResult { it }
            .onSuccess {
                // Returns the collection of search results.
                onSuccess.invoke(it)
            }
            .onError {
                //Unexpected error
                onError.invoke(it)
            }
    }
}