package com.example.mapapplication.originCode.net.api

import com.google.gson.annotations.SerializedName
import com.example.mapapplication.originCode.net.infrastructure.CollectionFormats.*
import com.example.mapapplication.originCode.net.models.GoogPlacesAutocompleteResponse
import com.example.mapapplication.originCode.net.models.PeliasResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchApi {

    /**
     * enum for parameter language
     */
    enum class LanguageGoogAutocomplete(val value: String) {
        @SerializedName(value = "bn")
        bn("bn"),

        @SerializedName(value = "en")
        en("en"),

        @SerializedName(value = "gu")
        gu("gu"),

        @SerializedName(value = "hi")
        hi("hi")
    }

    /**
     * Autocomplete API as per google api spec
     * The Place Autocomplete service is a web service that returns place predictions in response to an HTTP request.  The request specifies a textual search string and optional geographic bounds.  The service can be used to provide autocomplete functionality for text-based geographic searches,  by returning places such as businesses, addresses and points of interest as a user types.  The Place Autocomplete service can match on full words and substrings, resolving place names and addresses.  Applications can therefore send queries as the user types, to provide on-the-fly place predictions.  The returned predictions are designed to be presented to the user to aid them in selecting the desired place.  You can send a Place Details request for more information about any of the places which are returned.
     * Responses:
     *  - 200: 200 OK
     *  - 0: Unexpected error
     *
     * @param text The text string on which to search. The Place Autocomplete service will return candidate matches based on this string and order results based on their perceived relevance.
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
     * @return A Response object containing a GoogPlacesAutocompleteResponse. This response object contains the autocomplete suggestions from the Google Places API.
     */
    @GET("places/goog/autocomplete")
    suspend fun googAutocomplete(
        @Query("text") text: String,
        @Query("focus.point.lat") focusPointLat: Double? = null,
        @Query("focus.point.lon") focusPointLon: Double? = null,
        @Query("boundary.rect.min_lon") boundaryRectMinLon: Double? = null,
        @Query("boundary.rect.max_lon") boundaryRectMaxLon: Double? = null,
        @Query("boundary.rect.min_lat") boundaryRectMinLat: Double? = null,
        @Query("boundary.rect.max_lat") boundaryRectMaxLat: Double? = null,
        @Query("boundary.circle.lat") boundaryCircleLat: Double? = null,
        @Query("boundary.circle.lon") boundaryCircleLon: Double? = null,
        @Query("boundary.circle.radius") boundaryCircleRadius: Double? = null,
        @Query("boundary.country") boundaryCountry: List<String>? = null,
        @Query("boundary.gid") boundaryGid: String? = null,
        @Query("layers") layers: List<String>? = null,
        @Query("sources") sources: List<String>? = null,
        @Query("size") size: Int? = null,
        @Query("lang") lang: LanguageGoogAutocomplete? = LanguageGoogAutocomplete.en
    ): Response<GoogPlacesAutocompleteResponse>

    @GET("places/goog/autocomplete")
    suspend fun googAutocompleteType(
        @Query("text") text: String,
        @Query("type") type: String,
        @Query("focus.point.lat") focusPointLat: Double? = null,
        @Query("focus.point.lon") focusPointLon: Double? = null,
        @Query("boundary.rect.min_lon") boundaryRectMinLon: Double? = null,
        @Query("boundary.rect.max_lon") boundaryRectMaxLon: Double? = null,
        @Query("boundary.rect.min_lat") boundaryRectMinLat: Double? = null,
        @Query("boundary.rect.max_lat") boundaryRectMaxLat: Double? = null,
        @Query("boundary.circle.lat") boundaryCircleLat: Double? = null,
        @Query("boundary.circle.lon") boundaryCircleLon: Double? = null,
        @Query("boundary.circle.radius") boundaryCircleRadius: Double? = null,
        @Query("boundary.country") boundaryCountry: List<String>? = null,
        @Query("boundary.gid") boundaryGid: String? = null,
        @Query("layers") layers: List<String>? = null,
        @Query("sources") sources: List<String>? = null,
        @Query("size") size: Int? = null,
        @Query("lang") lang: LanguageGoogAutocomplete? = LanguageGoogAutocomplete.en
    ): Response<GoogPlacesAutocompleteResponse>


    /**
     * Retrieve details of a place using its GID.
     * Many search result components include an associated GID field (for example, an address may have a &#x60;localadmin_gid&#x60;). The place endpoint lets you look up these places directly by ID. Note that these IDs are not stable for all sources.
     * Responses:
     *  - 200: Returns the collection of search results.
     *  - 0: Unexpected error
     *
     * @param ids A list of Pelias GIDs to search for.
     * @param lang A BCP47 language tag which specifies a preference for localization of results. By default, results are in the default locale of the source data, but specifying a language will attempt to localize the results. Note that while a &#x60;langtag&#x60; (in RFC 5646 terms) can contain script, region, etc., only the &#x60;language&#x60; portion, an ISO 639 code, will be considered. So &#x60;en-US&#x60; and &#x60;en-GB&#x60; will both be treated as English. (optional)
     * @return [PeliasResponse]
     */
    @GET("places/place")
    suspend fun getPlaceDetails(
        @Query("ids") ids: CSVParams,
        @Query("lang") lang: String? = null
    ): Response<PeliasResponse>

    /**
     * Find places and addresses near geographic coordinates (reverse geocoding).
     * Reverse geocoding and search finds places and addresses near any geographic coordinates.
     * Responses:
     *  - 200: Returns the collection of search results.
     *  - 0: Unexpected error
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
     * @return [PeliasResponse]
     */
    @GET("places/reverse")
    suspend fun reverseGeocode(
        @Query("point.lat") pointLat: Double,
        @Query("point.lon") pointLon: Double,
        @Query("boundary.circle.radius") boundaryCircleRadius: Double? = null,
        @Query("layers") layers: CSVParams? = null,
        @Query("sources") sources: CSVParams? = null,
        @Query("boundary.country") boundaryCountry: CSVParams? = null,
        @Query("boundary.gid") boundaryGid: String? = null,
        @Query("size") size: Int? = null,
        @Query("lang") lang: String? = null
    ): Response<PeliasResponse>

    /**
     * Search for location and other info using a place name or address (forward geocoding).
     * The search endpoint lets you search for addresses, points of interest, and administrative areas. This is most commonly used for forward geocoding applications where you need to find the geographic coordinates of an address.
     * Responses:
     *  - 200: Returns the collection of search results.
     *  - 0: Unexpected error
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
     * @return [PeliasResponse]
     */
    @GET("places/search")
    suspend fun forwardGeocode(
        @Query("text") text: String,
        @Query("focus.point.lat") focusPointLat: Double? = null,
        @Query("focus.point.lon") focusPointLon: Double? = null,
        @Query("boundary.rect.min_lat") boundaryRectMinLat: Double? = null,
        @Query("boundary.rect.max_lat") boundaryRectMaxLat: Double? = null,
        @Query("boundary.rect.min_lon") boundaryRectMinLon: Double? = null,
        @Query("boundary.rect.max_lon") boundaryRectMaxLon: Double? = null,
        @Query("boundary.circle.lat") boundaryCircleLat: Double? = null,
        @Query("boundary.circle.lon") boundaryCircleLon: Double? = null,
        @Query("boundary.circle.radius") boundaryCircleRadius: Double? = null,
        @Query("boundary.country") boundaryCountry: CSVParams? = null,
        @Query("boundary.gid") boundaryGid: String? = null,
        @Query("layers") layers: CSVParams? = null,
        @Query("sources") sources: CSVParams? = null,
        @Query("size") size: Int? = null,
        @Query("lang") lang: String? = null
    ): Response<PeliasResponse>


}