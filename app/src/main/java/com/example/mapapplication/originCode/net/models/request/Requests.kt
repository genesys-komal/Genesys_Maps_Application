package com.example.mapapplication.originCode.net.models.request

import com.example.mapapplication.originCode.net.api.SearchApi
import com.example.mapapplication.originCode.net.infrastructure.CollectionFormats
import java.math.BigDecimal

data class GeocodeRequest(
    val pointLat: Double,
    val pointLon: Double,
    val boundaryCircleRadius: Double? = null,
    val layers: CollectionFormats.CSVParams? = null,
    val sources: CollectionFormats.CSVParams? = null,
    val boundaryCountry: CollectionFormats.CSVParams? = null,
    val boundaryGid: String? = null,
    val size: Int? = null,
    val lang: String? = null
)

data class AutoCompleteRequest(
    val input: String,
    val strictbounds: Boolean? = null,
    val offset: BigDecimal? = null,
    val origin: String? = null,
    val location: String? = null,
    val radius: BigDecimal? = null,
    val types: String? = null,
    val language: SearchApi.LanguageGoogAutocomplete? = SearchApi.LanguageGoogAutocomplete.en
)

data class ForwardGeocodeRequest(
    val text: String,
    val focusPointLat: Double? = null,
    val focusPointLon: Double? = null,
    val boundaryRectMinLat: Double? = null,
    val boundaryRectMaxLat: Double? = null,
    val boundaryRectMinLon: Double? = null,
    val boundaryRectMaxLon: Double? = null,
    val boundaryCircleLat: Double? = null,
    val boundaryCircleLon: Double? = null,
    val boundaryCircleRadius: Double? = null,
    val boundaryCountry: CollectionFormats.CSVParams? = null,
    val boundaryGid: String? = null,
    val layers: CollectionFormats.CSVParams? = null,
    val sources: CollectionFormats.CSVParams? = null,
    val size: Int? = null,
    val lang: String? = null
)