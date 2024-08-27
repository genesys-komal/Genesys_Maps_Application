package com.example.mapapplication.originCode.net.auth

import java.io.IOException
import java.net.URI
import java.net.URISyntaxException

import okhttp3.Interceptor
import okhttp3.Response

internal class ApiKeyAuth(
    private val location: String = "",
    private val paramName: String = "",
    private var apiKey: String = ""
) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        when (location) {
            "query" -> {
                var newQuery = request.url.toUri().query
                val paramValue = "$paramName=$apiKey"
                if (newQuery == null) {
                    newQuery = paramValue
                } else {
                    newQuery += "&$paramValue"
                }

                val newUri: URI
                try {
                    val oldUri = request.url.toUri()
                    newUri = URI(
                        oldUri.scheme, oldUri.authority,
                        oldUri.path, newQuery, oldUri.fragment
                    )
                } catch (e: URISyntaxException) {
                    throw IOException(e)
                }

                request = request.newBuilder().url(newUri.toURL()).build()
            }

            "header" -> {
                request = request.newBuilder()
                    .addHeader(paramName, apiKey)
                    .build()
            }

            "cookie" -> {
                request = request.newBuilder()
                    .addHeader("Cookie", "$paramName=$apiKey")
                    .build()
            }
        }
        return chain.proceed(request)
    }
}
