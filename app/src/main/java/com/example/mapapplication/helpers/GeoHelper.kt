package com.example.mapapplication.helpers

import com.example.mapapplication.originCode.GeoMaps
import com.example.mapapplication.originCode.GeoMapsConfiguration
import com.example.mapapplication.BuildConfig

class GeoHelper {

  /*  companion object {
        const val apiKey1 = "4FBC5BFE7E34C58F414519B43972C"
        const val apiUrl1 = "https://api.genesysmap.com/api/v1"
//        https://api.genesysmap.com/api/v1/tiles-pan-india/data/planet/9/359/228.pbf?api_key=4FBC5BFE7E34C58F414519B43972C
//        https://api.genesysmap.com/api/v1/tiles-pan-india/styles/streets/style.json?api_key=4FBC5BFE7E34C58F414519B43972C
    }*/

    var geoMapsApi: GeoMaps? = null
    val apiKey = BuildConfig.API_KEY
    val apiUrl = BuildConfig.BASE_URL
    val tilesUrl = "$apiUrl/tiles-pan-india/styles/streets/style.json?api_key=$apiKey"
    val satelliteUrl = "$apiUrl/tiles-pan-india/styles/satellite/style.json?api_key=$apiKey"
    val traffic = "$apiUrl/traffic/data/23/14/11724/7597.pbf?api_key=$apiKey"

    init {
        val configuration = GeoMapsConfiguration.Builder()
            .setApiKey(apiKey)
            .setBaseUrl(apiUrl)

        geoMapsApi = GeoMaps.getInstance()
        geoMapsApi?.initialize(configuration)
    }

    fun close() {
        GeoMaps.close()
    }
}