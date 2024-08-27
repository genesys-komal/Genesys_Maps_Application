package com.example.mapapplication.originCode

class GeoMapsConfiguration {

    class Builder {

        var apiKey: String = ""
        var baseUrl: String = ""

        fun setApiKey(apiKey: String): Builder {
            this.apiKey = apiKey
            return this
        }

        fun setBaseUrl(baseUrl: String): Builder {
            this.baseUrl = baseUrl
            return this
        }
    }
}